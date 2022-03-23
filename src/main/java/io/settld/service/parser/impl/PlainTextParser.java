package io.settld.service.parser.impl;


import io.settld.model.Statistic;
import io.settld.service.parser.Parser;
import io.settld.util.ParserUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.color.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;


/**
 * Just a simple PlainTextParser implementation of {@link Parser} interface
 * For new file formats we need to create their own implementations and develop the {@link io.settld.service.factory.ParserFactory} class
 */
public class PlainTextParser implements Parser {

    private File file;

    public PlainTextParser(File file) {
        this.file = file;
    }

    public Statistic getStatistic(String content) {
        int numOfDots = ParserUtils.numOfDotsCalculator(content);
        int numOfWords = ParserUtils.numOfWordsCalculator(content);
        String mostUsedWord = ParserUtils.mostUsedWord(content);
        return new Statistic(numOfDots, numOfWords, mostUsedWord);
    }

    public String parse() {

        try (PDDocument document = Loader.loadPDF(file)) {
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent()) {
                throw new IOException("You do not have permission to extract text");
            }

            PDFTextStripper stripper = new MyPdfStripper();

            // This example uses sorting, but in some cases it is more useful to switch it off,
            // e.g. in some files with columns where the PDF content stream respects the
            // column order.
            stripper.setSortByPosition(true);

            for (int p = 1; p <= document.getNumberOfPages(); ++p) {
                // Set the page interval to extract. If you don't, then all pages would be extracted.
                stripper.setStartPage(p);
                stripper.setEndPage(p);

                // get the content text of the page
                String text = stripper.getText(document);

                // do some nice output with a header
                String pageStr = String.format("page %d:", p);
                System.out.println(pageStr);
                for (int i = 0; i < pageStr.length(); ++i) {
                    System.out.print("-");
                }
                System.out.println();
                System.out.println(text.trim());
                System.out.println();

                // If the extracted text is empty or gibberish, please try extracting text
                // with Adobe Reader first before asking for help. Also read the FAQ
                // on the website:
                // https://pdfbox.apache.org/2.0/faq.html#text-extraction
            }
        }catch (IOException e){

        }

        return null;
    }


    class MyPdfStripper extends PDFTextStripper {
        public MyPdfStripper() throws IOException {
            addOperator(new SetStrokingColorSpace());
            addOperator(new SetNonStrokingColorSpace());
            addOperator(new SetStrokingDeviceCMYKColor());
            addOperator(new SetNonStrokingDeviceCMYKColor());
            addOperator(new SetNonStrokingDeviceRGBColor());
            addOperator(new SetStrokingDeviceRGBColor());
            addOperator(new SetNonStrokingDeviceGrayColor());
            addOperator(new SetStrokingDeviceGrayColor());
            addOperator(new SetStrokingColor());
            addOperator(new SetStrokingColorN());
            addOperator(new SetNonStrokingColor());
            addOperator(new SetNonStrokingColorN());
        }

        @Override
        protected void startPage(PDPage page) throws IOException {
            super.startPage(page);
            cropBox = page.getCropBox();
            pageLeft = cropBox.getLowerLeftX();
            beginLine();
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            float recentEnd = 0;
            for (TextPosition textPosition : textPositions) {
                String textHere = textPosition.toString();
                if (textHere.trim().length() == 0)
                    continue;

                float start = textPosition.getX();
                boolean spacePresent = endsWithWS | textHere.startsWith(" ");

                if (needsWS | spacePresent | Math.abs(start - recentEnd) > 1) {
                    int spacesToInsert = insertSpaces(chars, start, needsWS & !spacePresent);

                    for (; spacesToInsert > 0; spacesToInsert--) {
                        writeString(" ");
                        chars++;
                    }
                }

                writeString(textHere);
                chars += textHere.length();

                needsWS = false;
                endsWithWS = textHere.endsWith(" ");
                try {
                    recentEnd = getEndX(textPosition);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    throw new IOException("Failure retrieving endX of TextPosition", e);
                }
            }
        }

        @Override
        protected void writeLineSeparator() throws IOException {
            super.writeLineSeparator();
            beginLine();
        }

        @Override
        protected void writeWordSeparator() throws IOException {
            needsWS = true;
        }

        void beginLine() {
            endsWithWS = true;
            needsWS = false;
            chars = 0;
        }

        int insertSpaces(int charsInLineAlready, float chunkStart, boolean spaceRequired) {
            int indexNow = charsInLineAlready;
            int indexToBe = (int) ((chunkStart - pageLeft) / fixedCharWidth);
            int spacesToInsert = indexToBe - indexNow;
            if (spacesToInsert < 1 && spaceRequired)
                spacesToInsert = 1;

            return spacesToInsert;
        }

        float getEndX(TextPosition textPosition) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
            Field field = textPosition.getClass().getDeclaredField("endX");
            field.setAccessible(true);
            return field.getFloat(textPosition);
        }

        public float fixedCharWidth = 3;

        boolean endsWithWS = true;
        boolean needsWS = false;
        int chars = 0;

        PDRectangle cropBox = null;
        float pageLeft = 0;

    }


}

