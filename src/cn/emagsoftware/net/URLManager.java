package cn.emagsoftware.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class URLManager
{

    private URLManager()
    {
    }

    public static String concatParams(Map<String, String> params, String enc)
    {
        Set<Entry<String, String>> entrySet = params.entrySet();
        StringBuffer paramsBuff = new StringBuffer();
        for (Entry<String, String> entry : entrySet)
        {
            String key = entry.getKey();
            String value = entry.getValue();
            if (enc != null)
            {
                try
                {
                    key = URLEncoder.encode(key, enc);
                    value = URLEncoder.encode(value, enc);
                } catch (UnsupportedEncodingException e)
                {
                    throw new RuntimeException(e);
                }
            }
            paramsBuff.append(key.concat("=").concat(value).concat("&"));
        }
        String paramsStr = paramsBuff.toString();
        int length = paramsStr.length();
        if (length > 0)
            paramsStr = paramsStr.substring(0, length - 1);
        return paramsStr;
    }

}
