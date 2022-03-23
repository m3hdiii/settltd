package io.settld.service;

import io.settld.model.Statistic;
import io.settld.service.factory.ParserFactory;
import io.settld.service.parser.Parser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
@PropertySource(value = {"classpath:data.properties"})
public class Processor {

    private String inputFolder;
    private String outputFolder;
    private String processedFolder;
    private String unsupportedFolder;

    @Value("${root.folder.path}")
    private String folderPath;

    private static final int THREAD_POOL_SIZE = 10;
    private static String ERROR_FORMAT = "The system cannot create the [%s] directory, try to check your given path!";

    private final ParserFactory factory;
    private ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public Processor(ParserFactory factory) {
        this.factory = factory;


    }

    /**
     * The observer thread for watching the new file adding process is instantiate here by {@link Processor#folderObserver()} method
     * We also process the existing files if any by {@link Processor#processedFolder} method
     */
    public void init() {
        if (null == folderPath) {
            log.error("You should put the path where the PDF files are existing inside the properties file");
            exitApp();
        }

        createDirs();
        new Thread(() -> folderObserver()).start();
        new Thread(() -> processFiles(new File(inputFolder).listFiles())).start();
    }

    /**
     * Watch for any file creation within the [input] folder and forward them for processing!
     */
    private void folderObserver() {

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(inputFolder);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            WatchKey key;

            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    processFiles(new File(inputFolder, event.context().toString()));
                }
                key.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * It processes the given files using 10 worker-thread
     *
     * @param files
     */
    private void processFiles(File... files) {
        List<Future<FinalResult>> futures = new ArrayList<>();
        for (File fx : files) {
            Future<FinalResult> future = executor.submit(() -> {
                Parser parser = factory.getParser(fx);
                String content = parser.parse();
                Statistic statistic = parser.getStatistic(content);
                return new FinalResult(fx, content, statistic);
            });
            futures.add(future);

        }

        for (Future<FinalResult> fu : futures) {
            try {
                FinalResult fs = fu.get();
                writeIntoOutputFolder(fs);
                moveToProcessedFolder(fs.getFile());
                printTheResultInsideConsole(fs);
            } catch (IllegalArgumentException | ExecutionException e) {
                e.printStackTrace();
                String path = e.getCause().getMessage();
                if (new File(path).exists())
                    moveToUnsupportedFolder(new File(path));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeIntoOutputFolder(FinalResult fs) {
        String str = fs.getTextContent();
        File inputFile = fs.getFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + File.separator + StringUtils.stripFilenameExtension(inputFile.getName()) + ".txt"))) {
            writer.write(str);
        } catch (IOException e) {
            log.error("Error while creating into textFile", e);
        }
    }


    private void moveToUnsupportedFolder(File file) {
        file.renameTo(new File(unsupportedFolder, file.getName()));
    }

    private void moveToProcessedFolder(File file) {
        file.renameTo(new File(processedFolder, file.getName()));

        if (file.exists()) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                log.error("Can't be deleted", e);
            }
        }
    }

    private void printTheResultInsideConsole(FinalResult finalResult) {
        File file = finalResult.getFile();
        Statistic statistic = finalResult.getStatistic();
        String content = finalResult.getTextContent();
        StringBuilder sb = new StringBuilder(String.format("File [%s] processed..\n\n", file.getName()));
        if (statistic != null) {
            sb
                    .append(String.format("Number of words: [%d]\n", statistic.getNumOfWords()))
                    .append(String.format("Number of repeated [.] characters: [%d]\n", statistic.getNumOfDots()))
                    .append(String.format("Most repeated word: [%s]\n", statistic.getMostUsedWord()))
                    .append("****************************************");
        }
        sb
                .append(content);

        System.out.println(sb);
    }

    class FinalResult {
        private File file;
        private String textContent;
        private Statistic statistic;

        public FinalResult(File file, String textContent, Statistic statistic) {
            this.file = file;
            this.textContent = textContent;
            this.statistic = statistic;
        }

        public File getFile() {
            return file;
        }

        public String getTextContent() {
            return textContent;
        }

        public Statistic getStatistic() {
            return statistic;
        }
    }


    /**
     * <b>This creates a directory structure</b>
     * <ul>
     *     <li>ROOT: it is the main folder where all files should reside</li>
     *     <li>input: it is the folder where the pdf format or other formats reside</li>
     *     <li>output: it is the folder where processed data reside</li>
     *     <li>processed: it is the folder where all processed files move to</li>
     *     <li>unsupported: it is the folder where all unsupported formats reside</li>
     * </ul>
     */
    private void createDirs() {
        File baseFolder = new File(folderPath);
        dirMaker(baseFolder, String.format(ERROR_FORMAT, "ROOT"));

        String subFolderName = "input";
        File input = new File(folderPath, subFolderName);
        dirMaker(input, String.format(ERROR_FORMAT, subFolderName));
        inputFolder = input.getAbsolutePath();


        subFolderName = "output";
        File output = new File(folderPath, subFolderName);
        dirMaker(output, String.format(ERROR_FORMAT, subFolderName));
        outputFolder = output.getAbsolutePath();

        subFolderName = "processed";
        File processed = new File(folderPath, subFolderName);
        dirMaker(processed, String.format(ERROR_FORMAT, subFolderName));
        processedFolder = processed.getAbsolutePath();

        subFolderName = "unsupported";
        File unsupported = new File(folderPath, subFolderName);
        dirMaker(unsupported, String.format(ERROR_FORMAT, subFolderName));
        unsupportedFolder = unsupported.getAbsolutePath();

        //Copy yhe LINCOLN_CONTRACT.pdf to the input directory to trigger the processor
        copyTestData(inputFolder);
    }

    private void copyTestData(String inputFolder) {
        try {
            URL resource = getClass().getClassLoader().getResource("LINCOLN_CONTRACT.pdf");
            URI uri = resource.toURI();
            File src = new File(uri);
            File dest = new File(inputFolder + File.separator + src.getName());
            FileUtils.copyFile(src, dest);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static void dirMaker(File file, String errorCreating) {
        if (!file.exists()) {
            boolean dirCreated = file.mkdir();
            if (!dirCreated) {
                System.err.println(errorCreating + "\n");
                System.exit(1);
            }
        }
    }

    private void exitApp() {
        System.exit(1);
    }

}
