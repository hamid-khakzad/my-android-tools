/**
 * <p>ͨ�õĵ��뵽EXCEL�࣬����Ŭ�������̶�������ͨ��
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
     * <p>ֱ�Ӳ���һ���µ�Excel
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
     * <p>��Excelģ��Ļ����ϲ���һ��Excel
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
     * <p>��Ŀ��Excel��д������
     * 
     * @param data,��ͳ�������ݣ�ÿһ�����ݱ�����һ��Map
     * @param keys,ָ��������Map���м�������
     * @param beginRow,ָ���ӵڼ��п�ʼ����һ��Ϊ0
     * @param beginCol,ָ���ӵڼ��п�ʼ����һ��Ϊ0
     * @param containRowNumber,�Ƿ���ÿ�п�ͷ���к�
     * @param extraData,�������ݣ���key������һ������Ϊ2��int���飬�������һ��Ԫ�ر�ʾExcel�У��ڶ���Ԫ�ر�ʾExcel�У���ǰ��������Ѵ������Ӧ��valueֵ���뵽Excel��Ӧ��λ��
     * @throws Exception ��������쳣
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
                wwb.createSheet("��1ҳ", 0);
            }
            WritableSheet ws = wwb.getSheet(0);

            if (data != null)
            {
                for (int i = 0; i < data.size(); i++)
                {
                    Map<String, Object> v = data.get(i);
                    for (int j = 0; j < keys.length; j++)
                    {
                        // ��Excel�У���һ��������ʾ�У��ڶ�����ʾ��
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
            { // �������Ҫ���������
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
            // ���ڴ���д���ļ���
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
