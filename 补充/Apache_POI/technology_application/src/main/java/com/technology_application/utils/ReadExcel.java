package com.technology_application.utils;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jiaqi Lin
 * @date 2022/02/25 20:02
 **/

public class ReadExcel {
    public static List<List<String>> GetList(String url) throws Exception {
        File excelFile = new File(url);
        // 获取流
        FileInputStream is = new FileInputStream(excelFile);
        checkExcel(new File(url));
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook(is);
        // 获取工作表
        Sheet sheet = workbook.getSheetAt(0);
        List<List<String>> dataList = new ArrayList<List<String>>();
        // 获取内容
        // 获取全部的行数,通过表(sheet)获取
        int rows = sheet.getPhysicalNumberOfRows();
        // 因为第一行是标题，所以从1开始
        for (int rowNum = 1; rowNum < rows; rowNum++) {
            // 获取行
            Row row = sheet.getRow(rowNum);
            // 如果行不为空
            if (row != null) {
                // 获取所有的列
                List<String> cellList = new ArrayList<String>();
                int cells = row.getPhysicalNumberOfCells();
                for (int colNum = 0; colNum < cells; colNum++) {
                    // 获取内容
                    Cell cell = row.getCell(colNum);
                    if (cell != null) {
                        // 获取内容的数据类型
                        int cellType = cell.getCellType();
                        //判断单元格数据类型
                        String cellValue = "";
                        // 这里使用的是枚举类型，也可以使用数字
                        switch (cellType) {
                            case XSSFCell.CELL_TYPE_STRING://字符串
                                cellValue = cell.getStringCellValue();
                                break;
                            case XSSFCell.CELL_TYPE_BOOLEAN://布尔
                                cellValue = String.valueOf(cell.getBooleanCellValue());
                                break;
                            case XSSFCell.CELL_TYPE_BLANK://空
                                System.out.println("BLANK!");
                                break;
                            case XSSFCell.CELL_TYPE_NUMERIC://数字类型
                                // 数字类型分为：日期和数字
                                // 先判断是不是日期
                                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                                    // 如果是日期
                                    Date date = cell.getDateCellValue();
                                    cellValue = new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
                                } else {
                                    // 如果只是普通的数字
                                    // 为了防止数字过长时以科学计数法显示
                                    // 先将其转换为字符串
                                    cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                    cellValue = cell.toString();
                                }
                                break;
                            case XSSFCell.CELL_TYPE_ERROR://异常
                                System.out.println("ERROR!");
                                break;
                        }
                        cellList.add(cellValue);
                    }
                }
                dataList.add(cellList);
                System.out.println(dataList);
            }
        }
        // 关闭流
        is.close();
        return dataList;
    }

    public static void checkExcel(File file) throws Exception {
        String message = "该文件是EXCEL文件！";
        System.out.println(message);
        if (!file.exists()) {
            message = "文件不存在！";
            throw new Exception(message);
        }
        if (!file.isFile() || ((!file.getName().endsWith(".xls") && !file.getName().endsWith(".xlsx") && !file.getName().endsWith(".XLS") && !file.getName().endsWith(".XLSX")))) {
            message = "文件不是Excel";
            throw new Exception(message);
        }
        System.out.println(message);
    }
}
