package io.settld.config;

import io.settld.structure.MyPDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean(name = "struct1")
    public PDFTextStripper pdfStructureProvider() {
        PDFTextStripper stripper = null;
        try {
            stripper = new PDFTextStripper();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stripper;
    }

    @Bean(name = "struct2")
    public PDFTextStripper myPdfStructureProvider() {
        PDFTextStripper stripper = null;
        try {
            stripper = new MyPDFTextStripper();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stripper;
    }
}
