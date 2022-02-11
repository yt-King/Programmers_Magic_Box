package com.ytking.itextdemo;


import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @author 应涛
 * @date 2022/2/11
 * @function：
 */
@RestController
@RequestMapping("/itex")
public class ItextController {

    @PostMapping("/test")
    public String test() throws IOException {
        //使用系统本地字体，可以解决生成的pdf中无法显示中文问题，本处字体为宋体
        //在创建字体时直接使用即可解决中文问题
        PdfFontFactory.createFont("C:\\Windows\\Fonts\\simsun.ttc,0", PdfEncodings.IDENTITY_H);

        PdfWriter writer = new PdfWriter("/test.pdf");
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.add(new Paragraph("好 的!"));
        document.close();
        return "success";
    }
}
