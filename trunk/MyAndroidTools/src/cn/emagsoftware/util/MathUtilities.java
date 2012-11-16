package cn.emagsoftware.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Random;

/**
 * <p>关于数字运算等方面的抽象实用类
 * 
 * @author Wendell
 * @version 1.2
 */
public abstract class MathUtilities
{

    /**
     * <p>提供精确的十进制加法运算 <p>由于小精度的十进制数不能准确地被二进制数所表示，如0.1准确地表示为double应该是0.1000000000000000055511151231257827021181583404541015625，所以若以double直接进行十进制运算，结果有一定的不可预知性
     * <p>尽管在某些情况下，double直接运算的结果也在预料之内，这是因为运算的结果输出，在绝大多数情况下，调用了Double.toString或String.valueOf方法 该方法提供了一个损失一定位数精度的算法，确保已定义的double值在返回字符串形式时，仍然保持先前定义的形式不变。如果运算的结果精度也在该方法损失精度的范围之内，其结果将是理想的。不过，反之，结果将带来意外
     * 
     * @param v1 被加数
     * @param v2 加数
     * @return
     */
    public static double add(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * <p>提供精确的十进制减法运算 <p>由于小精度的十进制数不能准确地被二进制数所表示，如0.1准确地表示为double应该是0.1000000000000000055511151231257827021181583404541015625，所以若以double直接进行十进制运算，结果有一定的不可预知性
     * <p>尽管在某些情况下，double直接运算的结果也在预料之内，这是因为运算的结果输出，在绝大多数情况下，调用了Double.toString或String.valueOf方法 该方法提供了一个损失一定位数精度的算法，确保已定义的double值在返回字符串形式时，仍然保持先前定义的形式不变。如果运算的结果精度也在该方法损失精度的范围之内，其结果将是理想的。不过，反之，结果将带来意外
     * 
     * @param v1 被减数
     * @param v2 减数
     * @return
     */
    public static double sub(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * <p>提供精确的十进制乘法运算 <p>由于小精度的十进制数不能准确地被二进制数所表示，如0.1准确地表示为double应该是0.1000000000000000055511151231257827021181583404541015625，所以若以double直接进行十进制运算，结果有一定的不可预知性
     * <p>尽管在某些情况下，double直接运算的结果也在预料之内，这是因为运算的结果输出，在绝大多数情况下，调用了Double.toString或String.valueOf方法 该方法提供了一个损失一定位数精度的算法，确保已定义的double值在返回字符串形式时，仍然保持先前定义的形式不变。如果运算的结果精度也在该方法损失精度的范围之内，其结果将是理想的。不过，反之，结果将带来意外
     * 
     * @param v1 被乘数
     * @param v2 乘数
     * @return
     */
    public static double mul(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * <p>提供精确的十进制除法运算 <p>由于小精度的十进制数不能准确地被二进制数所表示，如0.1准确地表示为double应该是0.1000000000000000055511151231257827021181583404541015625，所以若以double直接进行十进制运算，结果有一定的不可预知性
     * <p>尽管在某些情况下，double直接运算的结果也在预料之内，这是因为运算的结果输出，在绝大多数情况下，调用了Double.toString或String.valueOf方法 该方法提供了一个损失一定位数精度的算法，确保已定义的double值在返回字符串形式时，仍然保持先前定义的形式不变。如果运算的结果精度也在该方法损失精度的范围之内，其结果将是理想的。不过，反之，结果将带来意外
     * 
     * @param v1 被除数
     * @param v2 除数
     * @param scale 小数点后保留几位
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
     * <p>提供精确的十进制除法运算 <p>由于小精度的十进制数不能准确地被二进制数所表示，如0.1准确地表示为double应该是0.1000000000000000055511151231257827021181583404541015625，所以若以double直接进行十进制运算，结果有一定的不可预知性
     * <p>尽管在某些情况下，double直接运算的结果也在预料之内，这是因为运算的结果输出，在绝大多数情况下，调用了Double.toString或String.valueOf方法 该方法提供了一个损失一定位数精度的算法，确保已定义的double值在返回字符串形式时，仍然保持先前定义的形式不变。如果运算的结果精度也在该方法损失精度的范围之内，其结果将是理想的。不过，反之，结果将带来意外
     * 
     * @param v1 被除数
     * @param v2 除数
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
     * <p>小数位数控制
     * 
     * @param v 需要操作的数字
     * @param scale 小数点后保留几位
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
     * <p>严格控制小数点的位数(如6.500)
     * 
     * @param v 需要操作的数字
     * @param scale 小数点后保留几位
     * @return 以字符串返回结果。之所以以字符串的形式返回，是因为若以double返回，小数点后的0将被省去， 如6.500即变为6.5,这不符合本函数的初衷。若要使返回double，可以使用round函数代替
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
     * <p>千分位
     * 
     * @param v 需要操作的数字
     * @return
     */
    public static String thousandth(double v)
    {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setGroupingUsed(true);
        return formatter.format(v);
    }

    /**
     * <p>获取随机数
     * 
     * @param range 获取随机数的范围
     * @return
     */
    public static int Random(int range)
    {
        return new Random().nextInt(range);
    }

}