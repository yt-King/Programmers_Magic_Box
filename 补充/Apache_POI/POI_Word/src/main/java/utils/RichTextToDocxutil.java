package utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author corey
 * @version 1.0
 * @date 2020/5/3 5:51 下午
 * @Desc 将富文本txt导出成word
 */
public class RichTextToDocxutil {
    /**
     * 导出富本框到docx
     */
    public static void outRichTextToDocx(String contents ,String outFilePath) {
        String content = txt2String(contents);
        InputStream inputStream=null;
        OutputStream out = null;
        try {
            // 输入富文本内容，返回字节数组
            byte[] result = HtmlToWord.resolveHtml(content);
            //输出文件
            out = new FileOutputStream(outFilePath);
            out.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 读取html文件的内容
     *
     * @param content 读取富文本
     * @return 返回文件内容
     */
    public static String txt2String(String content) {
        StringBuilder result = new StringBuilder();
        try {
            // 构造一个BufferedReader类来读取富文本
            result.append(System.lineSeparator()+content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
