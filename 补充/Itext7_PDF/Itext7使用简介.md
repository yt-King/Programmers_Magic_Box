# Itext7使用简介

官方操作文档->https://kb.itextsupport.com/home/it7kb/ebooks/itext-7-jump-start-tutorial-for-java/chapter-1-introducing-basic-building-blocks

## 1.依赖(gradle构建)

```yaml
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    //包含低层次常用的基础的函数------------------------------------------
    implementation group: 'com.itextpdf', name: 'kernel', version: '7.2.0'
    implementation group: 'com.itextpdf', name: 'io', version: '7.2.0'
    //包含高层次的函数
    implementation group: 'com.itextpdf', name: 'layout', version: '7.2.0'
    //有关AcorForms操作需要的函数库
    implementation group: 'com.itextpdf', name: 'forms', version: '7.2.0'
    //有关PDF/A（电子文档标准）的相关操作
    implementation group: 'com.itextpdf', name: 'pdfa', version: '7.2.0'
    //----------------------以上为常用jar包---------------------------
    //条形码相关
    implementation group: 'com.itextpdf', name: 'barcodes', version: '7.2.0'
    //电子签名
    implementation group: 'com.itextpdf', name: 'sign', version: '7.2.0'
 	//字体相关，主要解决中文不显示的问题
    testImplementation group: 'com.itextpdf', name: 'font-asian', version: '7.2.0'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## 2.基本构建块

### 2.1-helloword

从最简单的helloword开始

```java
    @PostMapping("/test")
    public String test() throws FileNotFoundException {
        PdfWriter writer = new PdfWriter("/xxxxx/xxxx/test.pdf");//带路径的文件名
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.add(new Paragraph("Hello World!"));
        document.close();
        return "success";
    }
```

- 创建`PdfWriter`实例，`PdfWriter`是一个可以写PDF文件的对象，它不需要了解它要写的pdf的实际内容是什么，`PdfWriter`不需要知道文档是什么，一旦文件结构完成，它就写不同的文件部分,不同的对象，构成一个有效的文档。`PdfWriter`的初始化参数可以是**文件名**或者**Stream**或**带路径的文件名**。

![image-20220211231155465](Itext7%E4%BD%BF%E7%94%A8%E7%AE%80%E4%BB%8B.images/image-20220211231155465.png)

- `PdfWriter`了解它需要写什么内容，因为它监听`PdfDocument`的动态。`PdfWriter`负责管理添加的内容，并把内容分布到不同的页面上，并跟踪有关页面内容的所有信息。
- `PdfDocument`和`PdfWriter`创建以后，我们把`PdfDocument`传入`Docment`，并对`Document`对象操作，`Document`就是我们的pdf对象
- 创建`Paragraph`，包含`"Hello World"`字符串，并把这个短语加入`Document`独享中
- 关闭`Document`。PDF文档创建完成

结果如下图所示：

![image-20220211230701265](Itext7%E4%BD%BF%E7%94%A8%E7%AE%80%E4%BB%8B.images/image-20220211230701265.png)

### 2.2-解决中文不显示问题

itext默认情况下是不显示中文的，我这里的做法是采用本地的字体库文件，版本为itext7.2.0，其余版本不知道是否适用。部署到服务器上的话可以看一下linux的字体库怎么调用。

```java
//使用系统本地字体，可以解决生成的pdf中无法显示中文问题，本处字体为宋体
//在创建字体时直接使用即可解决中文问题
PdfFont sysFont = PdfFontFactory.createFont("C:/Windows/Fonts/simsun.ttc,1", PdfEncodings.IDENTITY_H);
//。。。。。。。。。。
document.add(new Paragraph("好 的!").setFont(sysFont));//通过setfont方法设置自定义的字体就可以正常显示了
```

### 2.3-写入list

```java
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
```

效果展示：

![image-20220212210658185](Itext7%E4%BD%BF%E7%94%A8%E7%AE%80%E4%BB%8B.images/image-20220212210658185.png)

### 2.4-写入图片

```java
@PostMapping("/image")
public String image() throws FileNotFoundException, MalformedURLException {
    //创建基础模块
    PdfWriter writer = new PdfWriter(PATH + "\\image.pdf");
    PdfDocument pdf = new PdfDocument(writer);
    //document就是我们的pdf对象
    Document document = new Document(pdf);
    //生成图片对象
    Image image = new Image(ImageDataFactory.create("testImage.jpg"),200,400,200);
    Image image1 = new Image(ImageDataFactory.create("testImage.jpg")).setHeight(100);
    Paragraph p = new Paragraph("图片输出测试： ").setFont(sysFont)
        .add(image)
        .add(image1);
    document.add(p);
    document.close();
    return "success";
}
```

效果展示：

![image-20220212213129749](Itext7%E4%BD%BF%E7%94%A8%E7%AE%80%E4%BB%8B.images/image-20220212213129749.png)

创建图像对象时可以设置图像位置：

![image-20220212213318401](Itext7%E4%BD%BF%E7%94%A8%E7%AE%80%E4%BB%8B.images/image-20220212213318401.png)

不设置位置的话就是默认的衔接状态，也可以通过set方法设置图片的各种属性：

![image-20220212213447074](Itext7%E4%BD%BF%E7%94%A8%E7%AE%80%E4%BB%8B.images/image-20220212213447074.png)

### 2.5-创建table

```java
PdfWriter writer = new PdfWriter(dest);
PdfDocument pdf = new PdfDocument(writer);
Document document = new Document(pdf, PageSize.A4.rotate());//设置页面大小，rotate（）表示页面横向
document.setMargins(20, 20, 20, 20);//设置页边距
PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);//定义不同的字体类型
PdfFont bold = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);//定义不同的字体类型
Table table = new Table(new float[]{4, 1, 3, 4, 3, 3, 3, 3, 1});//每个数定义一个列的相对宽度。
table.setWidth(UnitValue.createPercentValue(100));//表的宽度相对于页面的可用宽度，在这种情况下，表将使用100% 的页面宽度，减去页边距。
//接下来的操作时通过读取一个csv文件里的内容填充到pdf的表格上
BufferedReader br = new BufferedReader(new FileReader(DATA));
String line = br.readLine();
//process ()方法，使用特定的字体将行添加到表中，并定义行是否包含标题行的内容。
process(table, line, bold, true);
while ((line = br.readLine()) != null) {
    process(table, line, font, false);
}
br.close();
document.add(table);
document.close();
```

```java
public void process(Table table, String line, PdfFont font, boolean isHeader) {
    StringTokenizer tokenizer = new StringTokenizer(line, ";");
    while (tokenizer.hasMoreTokens()) {
        if (isHeader) {
            table.addHeaderCell(
                new Cell().add(
                    new Paragraph(tokenizer.nextToken()).setFont(font)));
        } else {
            table.addCell(
                new Cell().add(
                    new Paragraph(tokenizer.nextToken()).setFont(font)));
        }
    }
}
```

效果图如下：

![image-20220212221431603](Itext7%E4%BD%BF%E7%94%A8%E7%AE%80%E4%BB%8B.images/image-20220212221431603.png)