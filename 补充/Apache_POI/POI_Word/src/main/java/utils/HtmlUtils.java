package utils;


import com.jshop.common.constant.CommonConStant;
import com.jshop.common.enums.ElementEnum;
import com.jshop.common.enums.TitleFontEnum;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class HtmlUtils {

    /**
     * 给document添加指定元素
     *
     * @param document
     */
    public static void addElement(Document document) {
        if (ObjectUtils.isEmpty(document)) {
            throw new NullPointerException("不允许为空的对象添加元素");
        }
        Elements elements = document.getAllElements();
        for (Element e : elements) {
            String attrName = ElementEnum.getValueByCode(e.tag().getName());
            if (!StringUtils.isEmpty(attrName)) {
                e.attr(CommonConStant.COMMONATTR, attrName);
            }
        }
    }


    /**
     * 将富文本内容写入到Word
     * 因富文本样式种类繁多，不能一一枚举，目前实现了H1、H2、H3、段落、图片、表格枚举
     *
     * @param ritchText 富文本内容
     * @param doc       需要写入富文本内容的Word 写入图片和表格需要用到
     * @param paragraph
     * @param
     */
    public static void resolveHtml(String ritchText, XWPFDocument doc, XWPFParagraph paragraph) {
        Document document = Jsoup.parseBodyFragment(ritchText, "UTF-8");
        try {
            // 添加固定元素
            HtmlUtils.addElement(document);
            Elements elements = document.select("[" + CommonConStant.COMMONATTR + "]");
            for (Element em : elements) {
                XmlCursor xmlCursor = paragraph.getCTP().newCursor();
                switch (em.attr(CommonConStant.COMMONATTR)) {
                    case "title":
                        break;
                    case "subtitle":
                        break;
                    case "imgurl":
                        String src = em.attr("src");
                        URL url = new URL(src);
                        URLConnection uc = url.openConnection();
                        InputStream inputStream = uc.getInputStream();
                        XWPFParagraph imgurlparagraph = doc.insertNewParagraph(xmlCursor);
                        ParagraphStyleUtil.setImageCenter(imgurlparagraph);
                        imgurlparagraph.createRun().addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, "图片.jpeg", Units.toEMU(150), Units.toEMU(150));
                        closeStream(inputStream);
                        File file = new File("picture.jpg");
                        boolean exists = file.exists();
                        if (exists) {
                            file.delete();
                        }
                        break;
                    case "imgbase64":
                        break;
                    case "table":
                        XWPFTable xwpfTable = doc.insertNewTbl(xmlCursor);
                        addTable(xwpfTable, em);
                        // 设置表格居中
                        ParagraphStyleUtil.setTableLocation(xwpfTable, "center");
                        // 设置内容居中
                        ParagraphStyleUtil.setCellLocation(xwpfTable, "CENTER", "center");
                        break;
                    case "h1":
                        XWPFParagraph h1paragraph = doc.insertNewParagraph(xmlCursor);
                        XWPFRun xwpfRun_1 = h1paragraph.createRun();
                        xwpfRun_1.setText(em.text());
                        //居中
                        ParagraphStyleUtil.setImageCenter(h1paragraph);
                        // 设置字体
                        ParagraphStyleUtil.setTitle(xwpfRun_1, TitleFontEnum.H1.getTitle());
                        break;
                    case "h2":
                        XWPFParagraph h2paragraph = doc.insertNewParagraph(xmlCursor);
                        XWPFRun xwpfRun_2 = h2paragraph.createRun();
                        xwpfRun_2.setText(em.text());
                        //居中
                        ParagraphStyleUtil.setImageCenter(h2paragraph);
                        // 设置字体
                        ParagraphStyleUtil.setTitle(xwpfRun_2, TitleFontEnum.H2.getTitle());
                        break;
                    case "h3":
                        XWPFParagraph h3paragraph = doc.insertNewParagraph(xmlCursor);
                        XWPFRun xwpfRun_3 = h3paragraph.createRun();
                        xwpfRun_3.setText(em.text());
                        // 设置字体
                        ParagraphStyleUtil.setTitle(xwpfRun_3, TitleFontEnum.H3.getTitle());
                        break;
                    case "paragraph":
                        XWPFParagraph paragraphd = doc.insertNewParagraph(xmlCursor);
                        // 设置段落缩进 4个空格
                        paragraphd.createRun().setText("    " + em.text());
                        break;
                    case "br":
                        XWPFParagraph br = doc.insertNewParagraph(xmlCursor);
                        XWPFRun run = br.createRun();
                        run.addBreak(BreakType.TEXT_WRAPPING);
                    case "h7":
                        XWPFParagraph h7paragraph = doc.insertNewParagraph(xmlCursor);
                        XWPFRun xwpfRun_7 = h7paragraph.createRun();
                        xwpfRun_7.setText(em.text());
                        //居左
                        ParagraphStyleUtil.AlignmentRight(h7paragraph);
                        // 设置字体
                        ParagraphStyleUtil.setTitle(xwpfRun_7, TitleFontEnum.H7.getTitle());
                    default:
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输入流
     *
     * @param closeables
     */
    public static void closeStream(Closeable... closeables) {
        for (Closeable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 将富文本的表格转换为Word里面的表格
     */
    private static void addTable(XWPFTable xwpfTable, Element table) {
        Elements trs = table.getElementsByTag("tr");
        // XWPFTableRow 第0行特殊处理
        int rownum = 0;
        for (Element tr : trs) {
            addTableTr(xwpfTable, tr, rownum);
            rownum++;
        }
    }


    /**
     * 将元素里面的tr 提取到 xwpfTabel
     */
    private static void addTableTr(XWPFTable xwpfTable, Element tr, int rownum) {
        Elements tds = tr.getElementsByTag("th").isEmpty() ? tr.getElementsByTag("td") : tr.getElementsByTag("th");
        XWPFTableRow row_1 = null;
        for (int i = 0, j = tds.size(); i < j; i++) {
            if (0 == rownum) {
                // XWPFTableRow 第0行特殊处理,
                XWPFTableRow row_0 = xwpfTable.getRow(0);
                if (i == 0) {
                    row_0.getCell(0).setText(tds.get(i).text());
                } else {
                    row_0.addNewTableCell().setText(tds.get(i).text());
                }
            } else {
                if (i == 0) {
                    // 换行需要创建一个新行
                    row_1 = xwpfTable.createRow();
                    row_1.getCell(i).setText(tds.get(i).text());
                } else {
                    row_1.getCell(i).setText(tds.get(i).text());
                }
            }
        }

    }
}
