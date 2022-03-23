package io.settld.service.factory;

import io.settld.service.parser.Parser;
import io.settld.service.parser.impl.PDFParser;
import io.settld.service.parser.impl.PlainTextParser;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class ParserFactory {

    private final PDFTextStripper structureStripper;

    public ParserFactory(@Qualifier("struct2") PDFTextStripper structureStripper) {
        this.structureStripper = structureStripper;

    }

    public Parser getParser(File file) {

        if (isPlainText(file)) {
            return new PlainTextParser(file);
        }

        if (isPDF(file)) {
            return new PDFParser(file, structureStripper);
        }

        //... Other implementations
        throw new IllegalArgumentException(file.getAbsolutePath());
    }

    /**
     * If ever in the future we needed the pdf file
     *
     * @param file
     * @return
     */
    private boolean isPDF(File file) {
        String mimeType = getMimeType(file);
        return mimeType == null ? false : mimeType.equals("application/pdf");
    }

    private boolean isPlainText(File file) {
        String mimeType = getMimeType(file);
        return mimeType == null ? false : mimeType.equals("text/plain");
    }


    /**
     * It finds the file mime-type, even if the file has no specific/clear extensions
     *
     * @param file
     * @return
     */
    private String getMimeType(File file) {
        Path path = file.toPath();
        try {
            String mimeType = Files.probeContentType(path);
            return mimeType;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
