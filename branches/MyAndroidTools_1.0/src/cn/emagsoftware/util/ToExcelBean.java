/**
 * <p>通用的导入到EXCEL类，该类努力在最大程度上做到通用
 * @author Wendell
 * @version 1.3
 * @date 2009.09.14
 */
package cn.emagsoftware.util;

import java.io.File;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ToExcelBean
{

    private File modelFile = null;
    private File newFile   = null;

    /**
     * <p>直接产生一个新的Excel
     * 
     * @param newFile
     */
    public ToExcelBean(File newFile)
    {
        if (newFile == null)
            throw new NullPointerException();
        this.newFile = newFile;
    }

    /**
     * <p>在Excel模板的基础上产生一个Excel
     * 
     * @param modelFile
     * @param newFile
     */
    public ToExcelBean(File modelFile, File newFile)
    {
        if (modelFile == null || newFile == null)
            throw new NullPointerException();
        this.modelFile = modelFile;
        this.newFile = newFile;
    }

    /**
     * <p>向目标Excel中写入数据
     * 
     * @param data,传统的行数据，每一行数据必须是一个Map
     * @param keys,指定行数据Map所有键的数组
     * @param beginRow,指定从第几行开始，第一行为0
     * @param beginCol,指定从第几列开始，第一列为0
     * @param containRowNumber,是否在每行开头加行号
     * @param extraData,特殊数据，其key必须是一个长度为2的int数组，该数组第一个元素表示Excel列，第二个元素表示Excel行，当前方法将会把此数组对应的value值填入到Excel相应的位置
     * @throws Exception 如果产生异常
     */
    public void process(List<Map<String, Object>> data, String[] keys, int beginRow, int beginCol, boolean containRowNumber, Map<int[], Object> extraData) throws Exception
    {
        Workbook rw = null;
        WritableWorkbook wwb = null;
        try
        {
            if (newFile.exists())
                newFile.delete();
            File parent = newFile.getParentFile();
            if (parent != null && !parent.exists())
                parent.mkdirs();
            if (modelFile != null)
            {
                rw = Workbook.getWorkbook(modelFile);
                wwb = Workbook.createWorkbook(newFile, rw);
            } else
            {
                wwb = Workbook.createWorkbook(newFile);
                wwb.createSheet("第1页", 0);
            }
            WritableSheet ws = wwb.getSheet(0);

            if (data != null)
            {
                for (int i = 0; i < data.size(); i++)
                {
                    Map<String, Object> v = data.get(i);
                    for (int j = 0; j < keys.length; j++)
                    {
                        // 在Excel中，第一个参数表示列，第二个表示行
                        int col = j + beginCol;
                        if (containRowNumber)
                        {
                            if (j == 0)
                            {
                                CellFormat format = ws.getCell(col, i + beginRow).getCellFormat();
                                if (format == null)
                                    format = new WritableCellFormat();
                                Number index = new Number(col, i + beginRow, i + 1, format);
                                ws.addCell(index);
                            }
                            col = col + 1;
                        }
                        Object valueObj = v.get(keys[j]);
                        CellFormat format = ws.getCell(col, i + beginRow).getCellFormat();
                        if (format == null)
                            format = new WritableCellFormat();
                        if (valueObj instanceof Byte)
                        {
                            Number numValue = new Number(col, i + beginRow, ((Byte) valueObj).byteValue(), format);
                            ws.addCell(numValue);
                        } else if (valueObj instanceof Short)
                        {
                            Number numValue = new Number(col, i + beginRow, ((Short) valueObj).shortValue(), format);
                            ws.addCell(numValue);
                        } else if (valueObj instanceof Integer)
                        {
                            Number numValue = new Number(col, i + beginRow, ((Integer) valueObj).intValue(), format);
                            ws.addCell(numValue);
                        } else if (valueObj instanceof Long)
                        {
                            Number numValue = new Number(col, i + beginRow, ((Long) valueObj).longValue(), format);
                            ws.addCell(numValue);
                        } else if (valueObj instanceof Float)
                        {
                            Number numValue = new Number(col, i + beginRow, ((Float) valueObj).floatValue(), format);
                            ws.addCell(numValue);
                        } else if (valueObj instanceof Double)
                        {
                            Number numValue = new Number(col, i + beginRow, ((Double) valueObj).doubleValue(), format);
                            ws.addCell(numValue);
                        } else if (valueObj instanceof BigDecimal)
                        {
                            Number numValue = new Number(col, i + beginRow, ((BigDecimal) valueObj).doubleValue(), format);
                            ws.addCell(numValue);
                        } else if (valueObj instanceof Boolean)
                        {
                            jxl.write.Boolean boolValue = new jxl.write.Boolean(col, i + beginRow, ((Boolean) valueObj).booleanValue(), format);
                            ws.addCell(boolValue);
                        } else
                        {
                            Label labValue = new Label(col, i + beginRow, StringUtilities.toStringWhenNull(valueObj, ""), format);
                            ws.addCell(labValue);
                        }
                    }
                }
            }
            if (extraData != null)
            { // 额外的需要填入的数据
                Iterator<int[]> allkeys = extraData.keySet().iterator();
                while (allkeys.hasNext())
                {
                    int[] xy = allkeys.next();
                    Object valueObj = extraData.get(xy);
                    CellFormat format = ws.getCell(xy[0], xy[1]).getCellFormat();
                    if (format == null)
                        format = new WritableCellFormat();
                    if (valueObj instanceof Byte)
                    {
                        Number numValue = new Number(xy[0], xy[1], ((Byte) valueObj).byteValue(), format);
                        ws.addCell(numValue);
                    } else if (valueObj instanceof Short)
                    {
                        Number numValue = new Number(xy[0], xy[1], ((Short) valueObj).shortValue(), format);
                        ws.addCell(numValue);
                    } else if (valueObj instanceof Integer)
                    {
                        Number numValue = new Number(xy[0], xy[1], ((Integer) valueObj).intValue(), format);
                        ws.addCell(numValue);
                    } else if (valueObj instanceof Long)
                    {
                        Number numValue = new Number(xy[0], xy[1], ((Long) valueObj).longValue(), format);
                        ws.addCell(numValue);
                    } else if (valueObj instanceof Float)
                    {
                        Number numValue = new Number(xy[0], xy[1], ((Float) valueObj).floatValue(), format);
                        ws.addCell(numValue);
                    } else if (valueObj instanceof Double)
                    {
                        Number numValue = new Number(xy[0], xy[1], ((Double) valueObj).doubleValue(), format);
                        ws.addCell(numValue);
                    } else if (valueObj instanceof BigDecimal)
                    {
                        Number numValue = new Number(xy[0], xy[1], ((BigDecimal) valueObj).doubleValue(), format);
                        ws.addCell(numValue);
                    } else if (valueObj instanceof Boolean)
                    {
                        jxl.write.Boolean boolValue = new jxl.write.Boolean(xy[0], xy[1], ((Boolean) valueObj).booleanValue(), format);
                        ws.addCell(boolValue);
                    } else
                    {
                        Label labValue = new Label(xy[0], xy[1], StringUtilities.toStringWhenNull(valueObj, ""), format);
                        ws.addCell(labValue);
                    }
                }
            }
            // 从内存中写入文件中
            wwb.write();
        } finally
        {
            try
            {
                if (wwb != null)
                    wwb.close();
            } finally
            {
                if (rw != null)
                    rw.close();
            }
        }
    }

}
