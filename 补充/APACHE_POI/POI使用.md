# POI的使用

POI提供了HSSF、XSSF以及SXSSF三种方式操作Excel。

**HSSF：**Excel97-2003版本，扩展名为.xls。一个sheet最大行数**65536**，最大列数256。

**XSSF：**Excel2007版本开始，扩展名为.xlsx。一个sheet最大行数**1048576**，最大列数16384。

**SXSSF：**是在XSSF基础上，POI3.8版本开始提供的**支持低内存占用**的操作方式，扩展名为.xlsx。

Excel版本兼容性是**向下兼容**。

三种类的接口及方法：

```text
HSSF：HSSFWorkbook、HSSFSheet、HSSFRow、HSSFCell……

XSSF：XSSFWorkbook、XSSFSheet、XSSFRow、XSSFCell……

SXSSF：SXSSFWorkbook、Sheet、Row、Cell……
```

很显然，**SXSSF比较特殊**，除了workBook带前缀外，其余均无前缀。而HSSF和XSSF都带了各自的前缀。所以设置格式等在相互之间切换还是很简单的。

**很重要的一点**，SXSSF之所以是一种低内存操作方式，是因为他的构造方法：

```java
SXSSFWorkbook w3= new SXSSFWorkbook(100);
```

这个100，可以理解为POI操作时，内存中最多只有100行数据，当超过这个数据时，就将内存之前的数据删除，并且会在硬盘中生成临时文件。从而保证了低内存消耗。当然，也可以将这个数字调大一点。

## POI 简介

Apache POI 是Apache的开源项目，POI提供API给Java程序对Microsoft Offic 格式文档读和写的功能。

## POI 结构说明

#### Excel包名称说明

|                          | HSSF                                                         | XSSF                                                         | SXSSF                                                        |
| ------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 对应Excel版本            | 2003及2003版本以前                                           | 2007后版本                                                   | 2007后版本                                                   |
| 扩展名                   | .xls                                                         | .xlsx                                                        | .xlsx                                                        |
| 区别                     | 最常用的方式，但是不能超过65535行，超出65536条就会报错，此方式因不足7万行所以一般不会内存溢出（OOM） | 突破HSSF 65535行的局限(1048576行，16384列)，不过就伴随着一个问题内存溢出，原因是你创建的是存在内存的并没有持久化 | POI 3.8 开始，提供基于XSSF低内存占用的SXSSF方式。它只会保存最新的excel rows在内存里供查看，在此之前的都会被写入到硬盘里。被写入硬盘的rows是不可见/不可访问的。只有内存里才可以访问到 |
| #### SXSSF与XSSF的对比： |                                                              |                                                              |                                                              |
|                          | XSSF                                                         | XSSF                                                         |                                                              |
| ---                      | ---                                                          | ---                                                          |                                                              |
| 访问量                   | 访问全部                                                     | 访问一定数量的数据                                           |                                                              |
| Sheet.clone()            | 支持                                                         | 不支持                                                       |                                                              |
| 公式求值                 | 支持                                                         | 不支持                                                       |                                                              |
| 更换表头                 | 可以                                                         | 不可，因为已经提前把数据写入硬盘就不能修改                   |                                                              |

#### 引入pom

```
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-excelant</artifactId>
            <version>4.1.0</version>
        </dependency>
复制代码
```

\####创建 HSSFWorkbook，

```java
        Workbook workbook = new HSSFWorkbook();
        try(OutputStream fileOut = new FileOutputStream("workbook.xls")){
            workbook.write(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
复制代码
```

#### 创建 XSSFWorkboook

```java
        Workbook workbook = new XSSFWorkbook();
        try(OutputStream fileOut = new FileOutputStream("workbook.xlsx")){
            workbook.write(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
复制代码
```

以上两种方法创建出来的文件是不能直接打开的，Excel最少要有一张Sheet 工作簿存在。

#### 创建Sheet

```java
        Workbook workbook = new HSSFWorkbook();
        /**
         * 请注意，工作表名称不能超过31个字符，且不能包含以下字符：
         * 0x0000、0x0003、冒号(:)、反斜杠(\)、星号(*)、问号(?)、正斜杠(/)、方括号([])
         * WorkbookUtil.createSafeSheetName(sheetName) 此方法可以将无效字符转化为空格
         * 已存在就不能创建，只能 workbook.getSheet("sheet")
         */
        Sheet sheet = workbook.createSheet("sheet");
        try(OutputStream fileOut = new FileOutputStream("workbook.xls")){
            workbook.write(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
复制代码
```

#### 创建单元格并设置单元格内容

```java
        Workbook workbook = new HSSFWorkbook();
        CreationHelper creationHelper = workbook.getCreationHelper();
        Sheet sheet = workbook.createSheet("sheet");

        //创建下标为0（首行，下标从0开始）行单元格(1行)，可以重复创建
        Row row = sheet.createRow(0);
        //创建0行下标为0的单元格(A1单元格)，可以重复创建
        Cell cell = row.createCell(0);
        //A1单元格设置内容为 1
        cell.setCellValue(1);

        //B1单元格设置内容为 1.2
        row.createCell(1).setCellValue(1.2);
        //C1单元格设置内容为 string
        row.createCell(2).setCellValue(
                creationHelper.createRichTextString("string"));
        //D1单元格设置内容为 true
        row.createCell(3).setCellValue(true);
        //E1单元格设置日期，无格式
        row.createCell(4).setCellValue(new Date());
        //设置单元格格式
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("m/d/yy h:mm"));
        //F1单元格设置日期并格式化
        Cell F1 = row.createCell(5);
        F1.setCellStyle(cellStyle);
        F1.setCellValue(new Date());
        //F1单元格设置日期并格式化
        Cell G1 = row.createCell(6);
        G1.setCellStyle(cellStyle);
        G1.setCellValue(Calendar.getInstance());
        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
复制代码
```

#### 设置不同类型的单元格

```java
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("sheet");

        Row row = sheet.createRow(2);
        row.createCell(0).setCellValue(1.1);
        row.createCell(1).setCellValue(new Date());
        row.createCell(2).setCellValue(Calendar.getInstance());
        row.createCell(3).setCellValue("a string");
        row.createCell(4).setCellValue(true);
        row.createCell(5).setCellType(CellType.ERROR);
        
        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
复制代码
```

#### 使用FIle或FileInputStream打开Excel，，只能打开已存在的Excel文件

```java
        /**
         * 当打开一个Excel 文件(.xls或者.xlsx)时，可以从File或者FileInputStream打开
         * 使用File可以减少内存的消耗，需要时间较长
         * 使用FileInputStream需要更多内存，因为必须缓冲这个文件
         */

        // 使用文件打开 Excel
        Workbook file = WorkbookFactory.create(new File("workbook.xls"));
        // 使用 InputStream 打开 Excel,需要时间较短
        Workbook inputStream = WorkbookFactory.create(new FileInputStream("workbook.xls"));
复制代码
```

#### 使用HSSFWorkbook或XSSFWorkbook，通常应通过POIFSFileSystem或者OPCPackage来控制生命周期（包括完成后关闭文件），只能打开已存在的Excel文件

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
        fileSystem.close();

        POIFSFileSystem fileSystem = new POIFSFileSystem(new FileInputStream("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
        fileSystem.close();

        OPCPackage opcPackage = OPCPackage.open(new File("workbook.xls"));
        XSSFWorkbook workbook = new XSSFWorkbook(pkg);
        opcPackage.close();

        OPCPackage opcPackage = OPCPackage.open(new FileInputStream("workbook.xls"));
        XSSFWorkbookworkbook = new XSSFWorkbook(pkg);
        opcPackage.close();
复制代码
```

#### 对齐方式

```java
    @org.junit.Test
    public void excelTest() throws IOException, InvalidFormatException {
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);

        //文件中已经存在 "sheet" ，直接获取不需要创建
        Sheet sheet = workbook.getSheet("sheet");
        Row row = sheet.createRow(2);
        //设置行高
        row.setHeightInPoints(30);

        createCell(workbook, row, 0, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
        createCell(workbook, row, 1, HorizontalAlignment.CENTER_SELECTION, VerticalAlignment.BOTTOM);
        createCell(workbook, row, 2, HorizontalAlignment.FILL, VerticalAlignment.CENTER);
        createCell(workbook, row, 3, HorizontalAlignment.GENERAL, VerticalAlignment.CENTER);
        createCell(workbook, row, 4, HorizontalAlignment.JUSTIFY, VerticalAlignment.JUSTIFY);
        createCell(workbook, row, 5, HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        createCell(workbook, row, 6, HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
        fileSystem.close();
    }

    /**
     * 创建一个单元格并以某种方式对齐
     *
     * @param workbook 文件
     * @param row Excel行对象
     * @param column Excel第几列
     * @param halign 水平对齐方式 [GENERAL（常规）,LEFT（左对齐）,CENTER（水平居中）,RIGHT（右对齐）,FILL（填充对齐）,JUSTIFY（两端对齐）,CENTER_SELECTION（跨列居中）,DISTRIBUTED（分散对齐）]
     * @param valign 垂直对齐方式 [TOP（顶部对齐）,CENTER（垂直居中）,BOTTOM（底部对齐）,JUSTIFY（两端对齐）,DISTRIBUTED（分散对齐）]
     */
    private static void createCell(Workbook workbook, Row row, int column, HorizontalAlignment halign, VerticalAlignment valign) {
        Cell cell = row.createCell(column);
        cell.setCellValue("Align It");
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(halign);
        cellStyle.setVerticalAlignment(valign);
        cell.setCellStyle(cellStyle);
    }
复制代码
```

#### 设置单元格边框

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);

        Sheet sheet = workbook.getSheet("sheet");
        Row row = sheet.createRow(1);
        Cell cell = row.createCell(1);
        cell.setCellValue(4);
        
        CellStyle style = workbook.createCellStyle();
        //设置下边框和颜色
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        //设置左边框和颜色
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.GREEN.getIndex());
        //设置右边框和颜色
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLUE.getIndex());
        //设置上边框边框和颜色
        style.setBorderTop(BorderStyle.MEDIUM_DASHED);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        //设置单元格风格
        cell.setCellStyle(style);
        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }

        workbook.close();
        fileSystem.close();
复制代码
```

#### 遍历单元格

```java
        //workbook.iterator()
        for (Sheet sheet : workbook) {
            //sheet.iterator() 遍历已创建的行，跳过空行
            for (Row row : sheet) {
                ///row.iterator() 遍历已创建的单元格，跳过空单元格
                for (Cell cell : row) {

                }
            }
        }
复制代码
```

#### 遍历所有单元格

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);

        Sheet sheet = workbook.getSheet("sheet");
        //获取有内容的首行下标，下标从0开始
        int rowStart = sheet.getFirstRowNum();
        //获取有内容的尾行下标，下标从0开始
        int rowEnd = sheet.getLastRowNum();
        for (int rowNum = rowStart; rowNum <= rowEnd; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                row = sheet.createRow(rowNum);
            }

            //空行的下标为-1，存在一条内容下标为0
            int firstColumn = row.getFirstCellNum();
            //空行下标为-1，下标从1开始
            int lastColumn = row.getLastCellNum();

            for (int cellNum = 0; cellNum < lastColumn; cellNum++) {
                Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell == null) {
                    cell = row.createCell(cellNum);
                }
            }
        }

        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
        fileSystem.close();
复制代码
```

#### 获得不同格式单元格的值

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
        DataFormatter formatter = new DataFormatter();
        Sheet sheet1 = workbook.getSheetAt(0);
        for (Row row : sheet1) {
            for (Cell cell : row) {
                CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                // 获取当前单元格的表示（例如 A1，B2）                
                System.out.print(cellRef.formatAsString() + " - ");
                // 获取单元格值并应用任何的数据格式 (Date, 0.00, 1.23e9, $1.23, etc)
                String text = formatter.formatCellValue(cell);
                System.out.println(text);
                // 或者，获取值并自动格式化
                switch (cell.getCellTypeEnum()) {
                    case STRING:
                        System.out.println(cell.getRichStringCellValue().getString());
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            System.out.println(cell.getDateCellValue());
                        } else {
                            System.out.println(cell.getNumericCellValue());
                        }
                        break;
                    case BOOLEAN:
                        System.out.println(cell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        System.out.println(cell.getCellFormula());
                        break;
                    case BLANK:
                    case ERROR:
                    case _NONE:
                    default:
                        System.out.println();
                }
            }
        }
        workbook.close();
        fileSystem.close();
复制代码
```

#### 填充和颜色

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
        Sheet sheet = workbook.getSheet("sheet");
        Row row = sheet.createRow(1);
        
        // 填充图案
        CellStyle style = workbook.createCellStyle();
        style.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
        style.setFillPattern(FillPatternType.BIG_SPOTS);
        Cell cell = row.createCell(1);
        cell.setCellValue("X");
        cell.setCellStyle(style);

        // 填充颜色
        style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell = row.createCell(2);
        cell.setCellValue("X");
        cell.setCellStyle(style);

        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }

        workbook.close();
        fileSystem.close();
复制代码
```

#### 合并单元格

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);

        Sheet sheet = workbook.getSheet("sheet");
        Row row = sheet.createRow(1);
        Cell cell = row.createCell(1);
        String s = "This is a test of merging";
        cell.setCellValue(s);

        //合并单元格(B2,C2) 已经合并了的单元格就不能再次合并
        sheet.addMergedRegion(new CellRangeAddress(
                1, //first row (0-based)
                1, //last row  (0-based)
                1, //first column (0-based)
                2  //last column  (0-based)
        ));

        //合并后居中
        cell = row.getCell(1);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setCellStyle(cellStyle);

        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }

        workbook.close();
        fileSystem.close();
复制代码
```

#### 字体设定

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
        Sheet sheet = workbook.getSheet("sheet");

        Row row = sheet.createRow(1);
        // Create a new font and alter it.
        Font font = workbook.createFont();
        //设置字号
        font.setFontHeightInPoints((short)24);
        //设置字体
        font.setFontName("Courier New");
        //设置黑体
        font.setBold(true);
        //设置斜体
        font.setItalic(true);
        //设置删除线
        font.setStrikeout(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        Cell cell = row.createCell(1);
        cell.setCellValue("This is a test of fonts");
        cell.setCellStyle(style);

        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
        fileSystem.close();
复制代码
```

#### 单元格换行

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
        Sheet sheet = workbook.getSheet("sheet");

        Row row = sheet.createRow(2);
        Cell cell = row.createCell(2);
        cell.setCellValue("Use \n with word wrap on to create a new line");
        //要使用换行符，要 wrap = true
        CellStyle cs = workbook.createCellStyle();
        cs.setWrapText(true);
        cell.setCellStyle(cs);
        //增加行高可以容量两行文本
        row.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
        //自适应列宽
        sheet.autoSizeColumn(2);
       
        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
        fileSystem.close();
复制代码
```

#### 小数处理

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
        Sheet sheet = workbook.getSheet("sheet");

        CellStyle style;
        DataFormat format = workbook.createDataFormat();
        Row row;
        Cell cell;
        int rowNum = 0;
        int colNum = 0;
        row = sheet.createRow(rowNum++);
        cell = row.createCell(colNum);
        cell.setCellValue(111.25);
        style = workbook.createCellStyle();
        //小数点后保留1位
        style.setDataFormat(format.getFormat("0.0"));
        cell.setCellStyle(style);
        row = sheet.createRow(rowNum++);
        cell = row.createCell(colNum);
        cell.setCellValue(111.25);
        style = workbook.createCellStyle();
        //小数点保留3位
        style.setDataFormat(format.getFormat("0.000"));
        cell.setCellStyle(style);

        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
        fileSystem.close();
复制代码
```

#### 筛选功能

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
        Sheet sheet = workbook.getSheet("sheet");

        // 设置默认列宽
        sheet.setDefaultColumnWidth(20); 
        HSSFFont font = workbook.createFont();
        font.setFontName("黑体");
        // 设置字体大小
        font.setFontHeightInPoints((short) 13);

        Row row = sheet.createRow((short) 0);
        row.createCell(0).setCellValue("员工编号");
        row.createCell(1).setCellValue("姓名");
        row.createCell(2).setCellValue("绩效等级");
        row.createCell(3).setCellValue("考核类型");

        for (int i = 1; i <= 5; i++) {
            row = sheet.createRow(i);
            row.createCell(0).setCellValue(String.valueOf((int) (Math.random() * 100)));
            row.createCell(1).setCellValue("Test" + i);
            row.createCell(2).setCellValue("B");
            row.createCell(3).setCellValue("月季考核");
        }

        //A1到D1 单元格筛选
        CellRangeAddress c = CellRangeAddress.valueOf("A1:D1");
        sheet.setAutoFilter(c);

        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
        fileSystem.close();
复制代码
```

#### 冻结单元格窗口

```java
        POIFSFileSystem fileSystem = new POIFSFileSystem(new File("workbook.xls"));
        HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
        Sheet sheet = workbook.getSheet("sheet");
        /**
         * cellNum:表示要冻结的列数, 从1开始 
         * rowNum:表示要冻结的行数，从1开始 
         * firstCellNum:表示被固定列右边第一列的列号，从0开始
         * firstRollNum :表示被固定行下边第一列的行号，从0开始
         * firstCellNum>=cellNum && firstRollNum >=cellNum
         */
        sheet.createFreezePane(int cellNum,int rowNum,int firstCellNum,int firstRollNum);
        
        try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
        fileSystem.close();
```

