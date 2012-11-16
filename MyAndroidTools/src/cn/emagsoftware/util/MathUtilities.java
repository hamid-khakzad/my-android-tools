package cn.emagsoftware.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Random;

/**
 * <p>������������ȷ���ĳ���ʵ����
 * 
 * @author Wendell
 * @version 1.2
 */
public abstract class MathUtilities
{

    /**
     * <p>�ṩ��ȷ��ʮ���Ƽӷ����� <p>����С���ȵ�ʮ����������׼ȷ�ر�������������ʾ����0.1׼ȷ�ر�ʾΪdoubleӦ����0.1000000000000000055511151231257827021181583404541015625����������doubleֱ�ӽ���ʮ�������㣬�����һ���Ĳ���Ԥ֪��
     * <p>������ĳЩ����£�doubleֱ������Ľ��Ҳ��Ԥ��֮�ڣ�������Ϊ����Ľ��������ھ����������£�������Double.toString��String.valueOf���� �÷����ṩ��һ����ʧһ��λ�����ȵ��㷨��ȷ���Ѷ����doubleֵ�ڷ����ַ�����ʽʱ����Ȼ������ǰ�������ʽ���䡣�������Ľ������Ҳ�ڸ÷�����ʧ���ȵķ�Χ֮�ڣ�������������ġ���������֮���������������
     * 
     * @param v1 ������
     * @param v2 ����
     * @return
     */
    public static double add(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * <p>�ṩ��ȷ��ʮ���Ƽ������� <p>����С���ȵ�ʮ����������׼ȷ�ر�������������ʾ����0.1׼ȷ�ر�ʾΪdoubleӦ����0.1000000000000000055511151231257827021181583404541015625����������doubleֱ�ӽ���ʮ�������㣬�����һ���Ĳ���Ԥ֪��
     * <p>������ĳЩ����£�doubleֱ������Ľ��Ҳ��Ԥ��֮�ڣ�������Ϊ����Ľ��������ھ����������£�������Double.toString��String.valueOf���� �÷����ṩ��һ����ʧһ��λ�����ȵ��㷨��ȷ���Ѷ����doubleֵ�ڷ����ַ�����ʽʱ����Ȼ������ǰ�������ʽ���䡣�������Ľ������Ҳ�ڸ÷�����ʧ���ȵķ�Χ֮�ڣ�������������ġ���������֮���������������
     * 
     * @param v1 ������
     * @param v2 ����
     * @return
     */
    public static double sub(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * <p>�ṩ��ȷ��ʮ���Ƴ˷����� <p>����С���ȵ�ʮ����������׼ȷ�ر�������������ʾ����0.1׼ȷ�ر�ʾΪdoubleӦ����0.1000000000000000055511151231257827021181583404541015625����������doubleֱ�ӽ���ʮ�������㣬�����һ���Ĳ���Ԥ֪��
     * <p>������ĳЩ����£�doubleֱ������Ľ��Ҳ��Ԥ��֮�ڣ�������Ϊ����Ľ��������ھ����������£�������Double.toString��String.valueOf���� �÷����ṩ��һ����ʧһ��λ�����ȵ��㷨��ȷ���Ѷ����doubleֵ�ڷ����ַ�����ʽʱ����Ȼ������ǰ�������ʽ���䡣�������Ľ������Ҳ�ڸ÷�����ʧ���ȵķ�Χ֮�ڣ�������������ġ���������֮���������������
     * 
     * @param v1 ������
     * @param v2 ����
     * @return
     */
    public static double mul(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * <p>�ṩ��ȷ��ʮ���Ƴ������� <p>����С���ȵ�ʮ����������׼ȷ�ر�������������ʾ����0.1׼ȷ�ر�ʾΪdoubleӦ����0.1000000000000000055511151231257827021181583404541015625����������doubleֱ�ӽ���ʮ�������㣬�����һ���Ĳ���Ԥ֪��
     * <p>������ĳЩ����£�doubleֱ������Ľ��Ҳ��Ԥ��֮�ڣ�������Ϊ����Ľ��������ھ����������£�������Double.toString��String.valueOf���� �÷����ṩ��һ����ʧһ��λ�����ȵ��㷨��ȷ���Ѷ����doubleֵ�ڷ����ַ�����ʽʱ����Ȼ������ǰ�������ʽ���䡣�������Ľ������Ҳ�ڸ÷�����ʧ���ȵķ�Χ֮�ڣ�������������ġ���������֮���������������
     * 
     * @param v1 ������
     * @param v2 ����
     * @param scale С���������λ
     * @return
     */
    public static double div(double v1, double v2, int scale)
    {
        if (v2 == 0)
            return 0;
        if (scale < 0)
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * <p>�ṩ��ȷ��ʮ���Ƴ������� <p>����С���ȵ�ʮ����������׼ȷ�ر�������������ʾ����0.1׼ȷ�ر�ʾΪdoubleӦ����0.1000000000000000055511151231257827021181583404541015625����������doubleֱ�ӽ���ʮ�������㣬�����һ���Ĳ���Ԥ֪��
     * <p>������ĳЩ����£�doubleֱ������Ľ��Ҳ��Ԥ��֮�ڣ�������Ϊ����Ľ��������ھ����������£�������Double.toString��String.valueOf���� �÷����ṩ��һ����ʧһ��λ�����ȵ��㷨��ȷ���Ѷ����doubleֵ�ڷ����ַ�����ʽʱ����Ȼ������ǰ�������ʽ���䡣�������Ľ������Ҳ�ڸ÷�����ʧ���ȵķ�Χ֮�ڣ�������������ġ���������֮���������������
     * 
     * @param v1 ������
     * @param v2 ����
     * @return
     */
    public static double div(double v1, double v2)
    {
        if (v2 == 0)
            return 0;
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.divide(b2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * <p>С��λ������
     * 
     * @param v ��Ҫ����������
     * @param scale С���������λ
     * @return
     */
    public static double round(double v, int scale)
    {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setGroupingUsed(false);
        formatter.setMaximumFractionDigits(scale);
        try
        {
            return formatter.parse(formatter.format(v)).doubleValue();
        } catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>�ϸ����С�����λ��(��6.500)
     * 
     * @param v ��Ҫ����������
     * @param scale С���������λ
     * @return ���ַ������ؽ����֮�������ַ�������ʽ���أ�����Ϊ����double���أ�С������0����ʡȥ�� ��6.500����Ϊ6.5,�ⲻ���ϱ������ĳ��ԡ���Ҫʹ����double������ʹ��round��������
     */
    public static String round2(double v, int scale)
    {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setGroupingUsed(false);
        formatter.setMaximumFractionDigits(scale);
        formatter.setMinimumFractionDigits(scale);
        return formatter.format(v);
    }

    /**
     * <p>ǧ��λ
     * 
     * @param v ��Ҫ����������
     * @return
     */
    public static String thousandth(double v)
    {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setGroupingUsed(true);
        return formatter.format(v);
    }

    /**
     * <p>��ȡ�����
     * 
     * @param range ��ȡ������ķ�Χ
     * @return
     */
    public static int Random(int range)
    {
        return new Random().nextInt(range);
    }

}