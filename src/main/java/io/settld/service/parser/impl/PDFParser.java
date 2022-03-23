package io.settld.service.parser.impl;

import io.settld.service.parser.Parser;
import io.settld.util.ParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;

@Slf4j
public class PDFParser implements Parser {

    private File file;
    private PDFTextStripper structureStripper;

    public PDFParser(File file, PDFTextStripper structureStripper) {
        this.file = file;
        this.structureStripper = structureStripper;
    }

    @Override
    public String parse() {
        StringBuilder sb = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(new File(file.getAbsolutePath()))) {
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent()) {
                log.error("You do not have permission to extract text");
                return null;
            }

            // This example uses sorting, but in some cases it is more useful to switch it off,
            structureStripper.setSortByPosition(true);
            structureStripper.setLineSeparator("\n");

            for (int p = 1; p <= document.getNumberOfPages(); ++p) {
                // Set the page interval to extract. If you don't, then all pages would be extracted.
                structureStripper.setStartPage(p);
                structureStripper.setEndPage(p);

                // let the magic happen
                String text = structureStripper.getText(document);
                text = ParserUtils.removeSpaceTabAndNewLine(text);

                // do some nice output with a header
                String pageStr = String.format("\n\n                Page %d", p);
                sb
                        .append(pageStr)
                        .append("\n")
                        .append(text.trim())
                        .append("\n");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
        String result = sb.toString();
        return result;
    }
}
