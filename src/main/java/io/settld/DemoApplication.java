package io.settld;


import io.settld.service.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication(scanBasePackages = "io.settld.*")
public class DemoApplication implements CommandLineRunner {

    private final Processor processor;
    @Value("${root.folder.path}")
    private String folderPath;

    public DemoApplication(Processor processor) {
        this.processor = processor;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(DemoApplication.class, args);
        Environment env = application.getEnvironment();
    }

    @Override
    public void run(String... args) throws Exception {
        processor.init();

    }
}
