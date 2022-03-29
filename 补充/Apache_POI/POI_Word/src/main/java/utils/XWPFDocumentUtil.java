package utils;


import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.util.List;
import java.util.Map;

/**
 * @author corey
 * @version 1.0
 * @date 2020/5/5 2:04 下午
 */
public class XWPFDocumentUtil {


    /**
     * 往doc的标记位置插入富文本内容 注意：目前支持富文本里面带url的图片，不支持base64编码的图片
     *
     * @param doc          需要插入内容的Word
     * @param ritchtextMap 标记位置对应的富文本内容
     * @param
     */
    public static void wordInsertRitchText(XWPFDocument doc, List<Map<String, Object>> ritchtextMap) {
        try {
            int i = 0;
            long beginTime = System.currentTimeMillis();
            // 如果需要替换多份富文本，通过Map来操作，key:要替换的标记，value：要替换的富文本内容
            for (Map<String, Object> mapList : ritchtextMap) {
                for (Map.Entry<String, Object> entry : mapList.entrySet()) {
                    i++;
                    for (XWPFParagraph paragraph : doc.getParagraphs()) {
                        if (entry.getKey().equals(paragraph.getText().trim())) {
                            // 在标记处插入指定富文本内容
                            HtmlUtils.resolveHtml(entry.getValue().toString(), doc, paragraph);
                            if (i == ritchtextMap.size()) {
                                //当导出最后一个富文本时 删除需要替换的标记
                                doc.removeBodyElement(doc.getPosOfParagraph(paragraph));
                            }
                            break;
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
