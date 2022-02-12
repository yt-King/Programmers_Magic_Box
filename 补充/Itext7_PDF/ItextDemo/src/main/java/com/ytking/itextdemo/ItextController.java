package com.ytking.itextdemo;


import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;

import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.StringTokenizer;

import static com.ytking.itextdemo.PathUtils.getAbsolutePathWithProject;

/**
 * @author 应涛
 * @date 2022/2/11
 * @function：
 */
@RestController
@RequestMapping("/itex")
public class ItextController {
    //文件存放路径--PATH = E:\banyun\javaInterview\补充\Itext7_PDF\ItextDemo
    final String PATH = getAbsolutePathWithProject();

    //使用系统本地字体，可以解决生成的pdf中无法显示中文问题，本处字体为宋体
    //在创建字体时直接使用即可解决中文问题
    PdfFont sysFont = PdfFontFactory.createFont("C:/Windows/Fonts/simsun.ttc,1", PdfEncodings.IDENTITY_H);

    public ItextController() throws IOException {
    }

    /**
     * 功能描述:
     * 测试输出，解决中文不显示问题
     *
     * @param
     * @return java.lang.String
     * @author yt
     * @date 2022/2/12 20:19
     */
    @PostMapping("/test")
    public String test() throws FileNotFoundException {
        //创建基础模块
        PdfWriter writer = new PdfWriter(PATH + "\\test.pdf");
        PdfDocument pdf = new PdfDocument(writer);
        //document就是我们的pdf对象
        Document document = new Document(pdf);
        document.add(new Paragraph("中文输出测试!").setFont(sysFont));
        document.close();
        return "success";
    }

    /**
     * 功能描述:
     * list测试
     *
     * @param
     * @return java.lang.String
     * @author yt
     * @date 2022/2/12 21:07
     */
    @PostMapping("/list")
    public String list() throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(PATH + "\\list.pdf");
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.add(new Paragraph("iText list:").setFont(sysFont));
        // 创建list，位于itextpdf.layout包下
        List list = new List()
                .setSymbolIndent(12)//列表项的缩进单元
                .setListSymbol("\u2022")//列表符号，没有找到具体说明，有需求只能一个一个试了
                .setFont(sysFont);
        // 添加数据
        list.add(new ListItem("English test1"))
                .add(new ListItem("English test2"))
                .add(new ListItem("中文测试1"))
                .add(new ListItem("中文测试2"));
        document.add(list);
        document.close();
        return "success";
    }

    /**
     * 功能描述:
     * 图片输出测试
     *
     * @param
     * @return java.lang.String
     * @author yt
     * @date 2022/2/12 21:53
     */
    @PostMapping("/image")
    public String image() throws FileNotFoundException, MalformedURLException {
        //创建基础模块
        PdfWriter writer = new PdfWriter(PATH + "\\image.pdf");
        PdfDocument pdf = new PdfDocument(writer);
        //document就是我们的pdf对象
        Document document = new Document(pdf);
        //生成图片对象
        Image image = new Image(ImageDataFactory.create("testImage.jpg"), 200, 400, 200);
        Image image1 = new Image(ImageDataFactory.create("testImage.jpg")).setHeight(100);
        Paragraph p = new Paragraph("图片输出测试： ").setFont(sysFont)
                .add(image)
                .add(image1);
        document.add(p);
        document.close();
        return "success";
    }


}
