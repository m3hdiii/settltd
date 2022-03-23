package io.settld.structure;

import io.settld.util.ParserUtils;
import org.apache.pdfbox.contentstream.operator.color.*;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

public class MyPDFTextStripper extends PDFTextStripper {

    public MyPDFTextStripper() throws IOException {

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
        this.setLineSeparator(this.LINE_SEPARATOR);
        this.setArticleStart(this.LINE_SEPARATOR);
        this.setArticleEnd(this.LINE_SEPARATOR);
    }

    protected void startArticle(boolean isLTR) throws IOException {


    }

    protected void endArticle() throws IOException {
        super.endArticle();

    }

    protected void writeString(String chars) throws IOException {
        super.writeString(escape(chars));
    }

    private static String escape(String chars) {
        return ParserUtils.removeSpaceTabAndNewLine(chars);
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        super.writeString("**------------START OF PAGE------------**\n");
        super.startPage(page);
    }

    @Override
    protected void endPage(PDPage page) throws IOException {
        super.writeString("**------------END OF PAGE------------**");
        super.endPage(page);
    }
}