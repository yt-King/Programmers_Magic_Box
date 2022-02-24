package com.ytking.itextdemo;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static com.ytking.itextdemo.PathUtils.getAbsolutePathWithProject;

/**
 * @author 应涛
 * @date 2022/2/18
    输入示例
    {
    "activityId":"asdasd",
    "activityName":"吊炸天项目研究",
    "activityTime":"2020-06-01 - 2020-13-31",
    "techDomain":"新材料、金属材料、金属及金属基复合新材料制备技术",
    "techFrom":"企业自有技术",
    "intellectualId":"asdasd",
    "budgetAll":"66666",
    "expenditureThree":"11111.03",
    "yearOne":"2018",
    "yearTwo":"2019",
    "yearThree":"2020",
    "yearPayOne":"0",
    "yearPayTwo":"0",
    "yearPayThree":"11111.03",
    "organizations":"立项目的：阿斯顿",
    "innovations":"x'x'x'x'xxxxxx",
    "results":"阿斯顿"
    }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevActivityInfoDao {
    /**
     * 活动编号
     */
    String activityId = "";
    /**
     * 研发活动名称
     */
    String activityName = "";
    /**
     * 起止时间
     */
    String activityTime = "";
    /**
     * 技术领域
     */
    String techDomain = "";
    /**
     * 技术来源
     */
    String techFrom = "";
    /**
     * 知识产权（编号）
     */
    String intellectualId = "";
    /**
     * 研发经费总预算（万元）
     */
    String budgetAll = "";
    /**
     * 研发经费近三年总支出（万元）
     */
    String expenditureThree = "";
    /**
     * 第一年年份
     */
    String yearOne = "";
    /**
     * 第二年年份
     */
    String yearTwo = "";
    /**
     * 第三年年份
     */
    String yearThree = "";
    /**
     * 第一年支出
     */
    String yearPayOne = "";
    /**
     * 第二年支出
     */
    String yearPayTwo = "";
    /**
     * 第三年支出
     */
    String yearPayThree = "";
    /**
     * 目的及组织实施方式(限400字)
     */
    String organizations = "";
    /**
     * 核心技术及创新点(限400字)
     */
    String innovations = "";
    /**
     * 取得的阶段性成果(限400字)
     */
    String results = "";

    String PATH = getAbsolutePathWithProject();

    public void ToPDF() throws IOException {

        PdfFont sysFont = PdfFontFactory.createFont("C:/Windows/Fonts/simsun.ttc,0", PdfEncodings.IDENTITY_H);
        //创建基础模块
        PdfWriter writer = new PdfWriter(PATH + "/test.pdf");
        PdfDocument pdf = new PdfDocument(writer);
        //生成操作对象
        Document document = new Document(pdf, PageSize.A4);//设置页面大小，rotate（）表示页面横向
        document.setMargins(20, 20, 20, 20);//设置页边距
        // 添加表格，7列
        Table table = new Table(new UnitValue[]{
                UnitValue.createPercentValue((float) 1.1),
                UnitValue.createPercentValue((float) 1),
                UnitValue.createPercentValue((float) 1.2),
                UnitValue.createPercentValue((float) 1),
                UnitValue.createPercentValue((float) 0.9),
                UnitValue.createPercentValue((float) 1),
                UnitValue.createPercentValue((float) 1)
        })//每个数定义一个列的相对宽度,用float数组定义不生效！！需要使用UnitValue。
                .setFont(sysFont)//设置全局字体
                .setWidthPercent(100)
                .setTextAlignment(TextAlignment.CENTER);//设置全局文字左右居中
//        table.setWidth(UnitValue.createPercentValue(100));//表的宽度相对于页面的可用宽度，在这种情况下，表将使用100% 的页面宽度，减去页边距。
        //第一行
        table.addCell(new Cell().add(new Paragraph("活动编号")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 6).add(new Paragraph(this.getActivityId())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        //第二行
        table.addCell(new Cell().add(new Paragraph("研发活动名称")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 3).add(new Paragraph(this.getActivityName())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell().add(new Paragraph("起止时间")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 2).add(new Paragraph(this.getActivityTime())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        //第三行
        table.addCell(new Cell().add(new Paragraph("技术领域")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 6).add(new Paragraph(this.getTechDomain())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        //第四行
        table.addCell(new Cell().add(new Paragraph("技术来源")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 2).add(new Paragraph(this.getTechFrom())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 2).add(new Paragraph("知识产权（编号）")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 2).add(new Paragraph(this.getIntellectualId())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        //第五行
        table.addCell(new Cell(3, 1).add(new Paragraph("研发经费总预算（万元）")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(3, 1).add(new Paragraph(this.getBudgetAll())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(3, 1).add(new Paragraph("研发经费近三年总支出（万元）")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(3, 1).add(new Paragraph(this.getExpenditureThree())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(3, 1).add(new Paragraph("其中")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell().add(new Paragraph(this.getYearOne())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell().add(new Paragraph(this.getYearPayOne())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell().add(new Paragraph(this.getYearTwo())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell().add(new Paragraph(this.getYearPayTwo())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell().add(new Paragraph(this.getYearThree())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell().add(new Paragraph(this.getYearPayThree())).setVerticalAlignment(VerticalAlignment.MIDDLE));
        //第一段
        table.addCell(new Cell().add(new Paragraph("目的及组织实施方式(限400字)")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 6).add(new Paragraph(this.getOrganizations()))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.LEFT));
        //第二段
        table.addCell(new Cell().add(new Paragraph("核心技术及创新点(限400字)")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 6).add(new Paragraph(this.getInnovations()))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.LEFT));
        ;
        //第三段
        table.addCell(new Cell().add(new Paragraph("取得的阶段性成果(限400字)")).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(new Cell(1, 6).add(new Paragraph(this.getResults()))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.LEFT));
        //添加table到pdf
        document.add(table);
        // 关闭文档
        document.close();
    }

    public boolean download(HttpServletResponse res) throws IOException {
        File file = new File(PATH + "/test.pdf");
        String fileName = "resylt.pdf";
        res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            os = res.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(file));
            int i = bis.read(buff);
            while (i != -1) {
                os.write(buff, 0, buff.length);
                os.flush();
                i = bis.read(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("success");
        return false;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setActivityTime(String activityTime) {
        this.activityTime = activityTime;
    }

    public void setTechDomain(String techDomain) {
        this.techDomain = techDomain;
    }

    public void setTechFrom(String techFrom) {
        this.techFrom = techFrom;
    }

    public void setIntellectualId(String intellectualId) {
        this.intellectualId = intellectualId;
    }

    public void setBudgetAll(String budgetAll) {
        this.budgetAll = budgetAll;
    }

    public void setExpenditureThree(String expenditureThree) {
        this.expenditureThree = expenditureThree;
    }

    public void setYearOne(String yearOne) {
        this.yearOne = yearOne;
    }

    public void setYearTwo(String yearTwo) {
        this.yearTwo = yearTwo;
    }

    public void setYearThree(String yearThree) {
        this.yearThree = yearThree;
    }

    public void setYearPayOne(String yearPayOne) {
        this.yearPayOne = yearPayOne;
    }

    public void setYearPayTwo(String yearPayTwo) {
        this.yearPayTwo = yearPayTwo;
    }

    public void setYearPayThree(String yearPayThree) {
        this.yearPayThree = yearPayThree;
    }

    public void setOrganizations(String organizations) {
        this.organizations = organizations;
    }

    public void setInnovations(String innovations) {
        this.innovations = innovations;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getActivityTime() {
        return activityTime;
    }

    public String getTechDomain() {
        return techDomain;
    }

    public String getTechFrom() {
        return techFrom;
    }

    public String getIntellectualId() {
        return intellectualId;
    }

    public String getBudgetAll() {
        return budgetAll;
    }

    public String getExpenditureThree() {
        return expenditureThree;
    }

    public String getYearOne() {
        return yearOne;
    }

    public String getYearTwo() {
        return yearTwo;
    }

    public String getYearThree() {
        return yearThree;
    }

    public String getYearPayOne() {
        return yearPayOne;
    }

    public String getYearPayTwo() {
        return yearPayTwo;
    }

    public String getYearPayThree() {
        return yearPayThree;
    }

    public String getOrganizations() {
        return organizations;
    }

    public String getInnovations() {
        return innovations;
    }

    public String getResults() {
        return results;
    }
}
