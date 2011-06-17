package cn.emagsoftware.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>�����ַ��������ʵ����
 * @author Wendell
 * @version 1.9
 */
public abstract class StringUtilities {
	
	/**
	 * <p>�����ֵĳ��ȳ���ָ���ĳ���ʱ����ȡ���ֲ����ʡ�Է���
	 * @param src
	 * @param ellipsis
	 * @param maxLength
	 * @return
	 */
	public static String addEllipsis(String src,String ellipsis,int maxLength){
		if(maxLength < 0) throw new IllegalArgumentException("maxLength could not less than zero");
		
		int srcLength = src.length();
		if(srcLength <= maxLength) return src;
		
		int ellipsisLength = ellipsis.length();
		int dis = srcLength + ellipsisLength - maxLength;
		if(dis < srcLength) return new StringBuffer(src.substring(0, srcLength-dis)).append(ellipsis).toString();
		else return ellipsis.substring(dis - srcLength);
	}
	
	/**
	 * <p>���е�src�а����ĵ�һ��contains
	 * @param src
	 * @param contains
	 * @return
	 */
	public static String cutWords(String src,String contains){
		int begin = src.indexOf(contains);
		if(begin == -1) return src;
		int end = begin + contains.length();
		String beginStr = "";
		if(begin != 0) beginStr = src.substring(0, begin);
		String endStr = "";
		if(end < src.length()) endStr = src.substring(end);
		return new StringBuffer(beginStr).append(endStr).toString();
	}
	
	/**
	 * <p>���е�src�а���������contains
	 * @param src
	 * @param contains
	 * @return
	 */
	public static String cutWordsAll(String src,String contains){
		String result = cutWords(src,contains);
		if(result.indexOf(contains) != -1) return cutWordsAll(result,contains);
		return result;
	}
	
	/**
	 * <p>��newValue�滻src�ַ����е�<b>��һ��</b>targetValue����String�������replaceFirst(String regex, String replacement)������ͬ���ǣ��÷���������������ʽ
	 * @param src
	 * @param targetValue
	 * @param newValue
	 * @return src�ַ�����һ������
	 */
	public static String replaceWords(String src, String targetValue, String newValue) {
		if (targetValue.equals("")) {
			throw new IllegalArgumentException("Old pattern must have content.");
		}
		final StringBuffer result = new StringBuffer();
		int startIdx = 0;
		int idxOld = src.indexOf(targetValue, startIdx);
		if (idxOld >= 0) {
			result.append(src.substring(startIdx, idxOld));
			result.append(newValue);
			startIdx = idxOld + targetValue.length();
		}
		result.append(src.substring(startIdx));
		return result.toString();
	}

	/**
	 * <p>��newValue�滻src�ַ����е�<b>����</b>targetValue����String�������replace(CharSequence target, CharSequence replacement)������ͬ����Ϊ������JDK1.5֮����ṩ�����Ը÷����ṩ��֮ǰ�İ汾ʹ��
	 * @param src
	 * @param targetValue
	 * @param newValue
	 * @return src�ַ�����һ������
	 */
	public static String replaceWordsAll(String src, String targetValue,String newValue) {
		if (targetValue.equals("")) {
			throw new IllegalArgumentException("Old pattern must have content.");
		}
		final StringBuffer result = new StringBuffer();
		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = src.indexOf(targetValue, startIdx)) >= 0) {
			result.append(src.substring(startIdx, idxOld));
			result.append(newValue);
			startIdx = idxOld + targetValue.length();
		}
		result.append(src.substring(startIdx));
		return result.toString();
	}
	
	/**
	 * <p>����src��toString()���ؽ������srcΪnull����Ϊnull������newValue
	 * @param src
	 * @param newValue
	 * @return
	 */
	public static String toStringWhenNull(Object src,String newValue){
		if(src == null) return newValue;
		String srcStr = src.toString();
		if(srcStr == null) return newValue;
		return srcStr;
	}
	
	/**
	 * <p>����src��toString()���ؽ������srcΪnull����Ϊnull���գ�����newValue
	 * @param src
	 * @param newValue
	 * @return
	 */
	public static String toStringWhenNullOrEmpty(Object src,String newValue){
		if(src == null) return newValue;
		String srcStr = src.toString();
		if(srcStr == null || srcStr.equals("")) return newValue;
		return srcStr;
	}
	
	/**
	 * <p>����src��toString()���ؽ������srcΪnull����Ϊnull���ա��ո񣬷���newValue
	 * @param src
	 * @param newValue
	 * @return
	 */
	public static String toStringWhenNullOrEmptyOrSpace(Object src,String newValue){
		if(src == null) return newValue;
		String srcStr = src.toString();
		if(srcStr == null || srcStr.trim().equals("")) return newValue;
		return srcStr;
	}
	
	/**
	 * <p>�жϸ������ַ����������ַ��Ƿ�Ϊ����
	 * @param str
	 * @return
	 */
	public static boolean isAllCharDigit(String str){
		int length = str.length();
		if(length == 0) return false;
		for (int i = 0; i < length; i++) {
			if(!Character.isDigit(str.charAt(i))) return false;
		}
		return true;
	}
	
	/**
	 * <p>�ϸ��жϸ������ַ����Ƿ�Ϊ����(������,0,������)
	 * @param str
	 * @return
	 */
	public static boolean isIntegral(String str) {
		if(str.startsWith("-")){
			if(str.length() == 1) return false;
			str = str.substring(1);
		}
		if(str.startsWith("0") && str.length() > 1) return false;
		return isAllCharDigit(str);
	}
	
	/**
	 * <p>�ϸ��жϸ������ַ����Ƿ�Ϊ��ֵ(����,С��)
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str){
		int index = str.indexOf(".");
		if(index == -1) return isIntegral(str);
		if(index == 0 || index == str.length()-1) return false;
        String num1 = str.substring(0,index);
        String num2 = str.substring(index+1);
		return isIntegral(num1) && isAllCharDigit(num2);
	}
	
	/**
	 * <p>�жϸ������ַ����Ƿ�������
	 * @param date
	 * @return
	 */
	public static boolean isDate(String date){
		String format = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
		Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
	}
	
	/**
	 * <p>�жϸ������ַ����Ƿ�������
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email){
        String format = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(email);
        if (matcher.find()) return true;
        else return false;
	}
	
	/**
	 * <p>��CSV��ʽ����ָ�����ַ�������������Ϣ���䷽����һ������
	 * @param strs
	 * @return
	 */
	public static String concatByCSV(String[] strs) {
		StringBuffer value = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			String filterValue = new String(strs[i]);
			if (filterValue.indexOf(",") != -1) {
				if (filterValue.indexOf("\"") != -1) {
					filterValue = filterValue.replaceAll("\"", "\"\"");
				}
				StringBuffer temp = new StringBuffer();
				temp.append("\"");
				temp.append(filterValue);
				temp.append("\"");
				filterValue = temp.toString();
			} else if (filterValue.indexOf("\"") != -1) {
				filterValue = filterValue.replaceAll("\"", "\"\"");
				StringBuffer temp = new StringBuffer();
				temp.append("\"");
				temp.append(filterValue);
				temp.append("\"");
				filterValue = temp.toString();
			}
			value.append(filterValue);
			if (i != strs.length - 1)
				value.append(",");
		}
		return value.toString();
	}

	/**
	 * <p>������CSV��ʽ���ӵ��ַ�������������Ϣ���䷽����һ������
	 * @param str
	 * @return
	 */
	public static String[] parseFromCSV(String str) {
		List<String> lk = new LinkedList<String>();
		String regex = "\\G(?:^|,)(?:\"([^\"]*+(?:\"\"[^\"]*+)*+)\"|([^\",]*+))";
		Matcher main = Pattern.compile(regex).matcher(str);
		Matcher mquote = Pattern.compile("\"\"").matcher("");
		while (main.find()) {
			String field;
			if (main.start(2) >= 0) {
				field = main.group(2);
			} else {
				field = mquote.reset(main.group(1)).replaceAll("\"");
			}
			lk.add(field);
		}
		Object[] os = lk.toArray();
		String[] ss = new String[os.length];
		for (int i = 0; i < os.length; i++) {
			ss[i] = (String) os[i];
		}
		return ss;
	}
	
	/**
	 * <p>���ֽ�����ת����16�����ַ���
	 * @param bArray
	 * @return
	 */
	public static String bytesToHexString(byte[] bArray){
	    StringBuffer sb = new StringBuffer(bArray.length);
	    String sTemp;
	    for (int i = 0; i < bArray.length; i++) {
	    	sTemp = Integer.toHexString(0xFF & bArray[i]);
	    	if (sTemp.length() < 2)
	    		sb.append(0);
	    	sb.append(sTemp.toUpperCase());
	    }
	    return sb.toString();
	}
	
	/**
	 * <p>��16�����ַ���ת�����ֽ�����
	 * @param hex
	 * @return
	 */
	public static byte[] hexStringToBytes(String hex){
	    int len = (hex.length() / 2);
	    byte[] result = new byte[len];
	    char[] achar = hex.toCharArray();
	    for (int i = 0; i < len; i++) {
	    	int pos = i * 2;
	    	byte b1 = (byte)"0123456789ABCDEF".indexOf(achar[pos]);
	    	byte b2 = (byte)"0123456789ABCDEF".indexOf(achar[pos + 1]);
	    	result[i] = (byte) (b1 << 4 | b2);
	    }
	    return result;
	}
	
}
