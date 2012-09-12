package cn.emagsoftware.cmcc.wlan;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.htmlparser.util.ParserException;

import cn.emagsoftware.net.http.HttpResponseResult;

public class AutoUser extends User
{

    protected static final String KEYWORD_APPLYPWDRES = "applypwd_res";

    public AutoUser()
    {
    }

    /**
     * <p>无论成功与否都会返回字符串描述，而不会像登录那样如果成功返回null
     * 
     * @return
     * @throws IOException
     * @throws ParserException
     */
    public String requestPassword() throws IOException, ParserException
    {
        // TODO Auto-generated method stub
        if (super.userName == null)
            throw new IllegalStateException("userName can not be null.");
        if (isLogged())
            throw new IllegalStateException("requestPassword() can only be called before logining.");
        parseLoginPage(this.cmccPortalHtml);
        String action = cmccLoginUrl;
        if (action == null || action.trim().length() == 0)
            action = CMCC_PORTAL_URL;
        Map<String, String> pageFields = new HashMap<String, String>();
        pageFields.put(INDICATOR_LOGIN_USERNAME, super.userName);
        pageFields.put(INDICATOR_LOGIN_ACTIONTYPE, "APPLYPWD");
        HttpResponseResult result = doHttpPostContainsRedirect(action, pageFields);
        String html = result.getDataString("gb2312");
        String keywordPwdRes = KEYWORD_CMCCCS + SEPARATOR + KEYWORD_APPLYPWDRES + SEPARATOR;
        int keywordIndex = html.indexOf(keywordPwdRes);
        if (keywordIndex == -1)
            throw new ParserException("can not find keyword from password response.");
        String subHtml = html.substring(keywordIndex + keywordPwdRes.length());
        int start = subHtml.indexOf(SEPARATOR);
        if (start == -1)
            throw new ParserException("can not find the begin separator from password response.");
        subHtml = subHtml.substring(start + SEPARATOR.length());
        int end = subHtml.indexOf(SEPARATOR);
        if (end == -1)
            throw new ParserException("can not find the end separator from password response.");
        return subHtml.substring(0, end);
    }

    @Override
    protected String doLogin() throws IOException, ParserException
    {
        // TODO Auto-generated method stub
        cmccLoginPageFields.put(INDICATOR_LOGIN_PWDTYPE, "2");
        return super.doLogin();
    }

}
