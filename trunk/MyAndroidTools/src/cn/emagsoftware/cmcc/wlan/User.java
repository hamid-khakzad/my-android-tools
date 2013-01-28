package cn.emagsoftware.cmcc.wlan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import cn.emagsoftware.net.http.HttpConnectionManager;
import cn.emagsoftware.net.http.HttpResponseResult;
import cn.emagsoftware.util.LogManager;

public class User
{

    protected static final String KEYWORD_CMCCCS               = "cmcccs";
    protected static final String KEYWORD_LOGINREQ             = "login_req";
    protected static final String KEYWORD_LOGINRES             = "login_res";
    protected static final String KEYWORD_OFFLINERES           = "offline_res";
    protected static final String SEPARATOR                    = "|";
    protected static final String CMCC_PORTAL_URL              = "https://221.176.1.140/wlan/index.php";
    // for redirection
    protected static final String INDICATOR_REDIRECT_PORTALURL = "portalurl";
    protected static final String INDICATOR_LOGIN_AC_NAME      = "wlanacname";
    protected static final String INDICATOR_LOGIN_USER_IP      = "wlanuserip";
    // form parameters in cmcc logining page
    protected static final String CMCC_LOGINPAGE_FORMNAME      = "loginform";
    protected static final String INDICATOR_LOGIN_ACTIONTYPE   = "actiontype";
    protected static final String INDICATOR_LOGIN_USERNAME     = "USER";
    protected static final String INDICATOR_LOGIN_PASSWORD     = "PWD";
    protected static final String INDICATOR_LOGIN_PWDTYPE      = "pwdtype";
    protected static final String INDICATOR_LOGIN_FORCEFLAG    = "forceflag";

    protected String              userName                     = null;
    protected String              password                     = null;

    protected boolean             isCancelLogin                = false;
    protected String              cmccPortalHtml               = null;
    protected String              cmccLoginUrl                 = null;
    protected Map<String, String> cmccLoginPageFields          = new HashMap<String, String>();

    public User()
    {
    }

    public void setUserName(String userName)
    {
        if (userName == null)
            throw new NullPointerException();
        this.userName = userName;
    }

    public void setPassword(String password)
    {
        if (password == null)
            throw new NullPointerException();
        this.password = password;
    }

    /**
     * <p>返回null表示成功，否则返回失败消息
     * 
     * @return
     * @throws IOException
     * @throws ParserException
     */
    public String login() throws IOException, ParserException
    {
        // TODO Auto-generated method stub
        isCancelLogin = false;
        if (isCancelLogin)
            return "CANCELLED"; // 判断是否已取消登录
        if (!redirectedToPortal()) // 未能重定向到portal页面将抛出异常，因为无法初始化参数
            throw new IllegalStateException("redirected to portal failed,already logged or using other network.");
        if (isCancelLogin)
            return "CANCELLED"; // 判断是否已取消登录
        parseLoginPage(this.cmccPortalHtml); // 解析CMCC登录页面
        if (isCancelLogin)
            return "CANCELLED"; // 判断是否已取消登录
        return doLogin(); // 模拟提交表单，验证当前帐户
    }

    protected void parseLoginPage(String pageHtml) throws ParserException, IOException
    {
        String keywordLoginReq = KEYWORD_CMCCCS + SEPARATOR + KEYWORD_LOGINREQ;
        if (pageHtml.indexOf(keywordLoginReq) != -1)
        { // 当前页面已经是CMCC登录页面
            doParseLoginPage(pageHtml);
        } else
        {
            String formatHtml = removeComment(pageHtml);
            String location = extractPortalUrl(formatHtml);
            if (location == null)
            {
                location = extractHref(formatHtml);
                if (location == null)
                {
                    location = extractNextUrl(formatHtml);
                    if (location == null || location.trim().length() == 0)
                    {
                        throw new ParserException("can not find the location after executing extractNextUrl().");
                    }
                }
            }
            HttpResponseResult result = doHttpGet(location);
            parseLoginPage(result.getDataString("gb2312"));
        }
    }

    protected void doParseLoginPage(String loginPageHtml) throws ParserException
    {
        String formatHtml = removeComment(loginPageHtml);
        Parser mHtmlParser = Parser.createParser(formatHtml, "gb2312");
        FormFilter filter = new FormFilter(CMCC_LOGINPAGE_FORMNAME);
        NodeList formList = mHtmlParser.parse(filter);
        if (formList == null || formList.size() == 0)
            throw new ParserException("could not find the form named '" + CMCC_LOGINPAGE_FORMNAME + "'");
        Node tag = formList.elementAt(0);
        FormTag formTag = (FormTag) tag;
        // 获取提交表单的URL
        String formAction = formTag.getFormLocation();
        if (formAction == null || formAction.trim().length() == 0)
        {
            this.cmccLoginUrl = null;
        } else
        {
            this.cmccLoginUrl = formAction.trim();
        }
        // 获取表单元素
        cmccLoginPageFields.clear();
        NodeList inputTags = formTag.getFormInputs();
        for (int j = 0; j < inputTags.size(); j++)
        {
            Node node = inputTags.elementAt(j);
            InputTag input = (InputTag) node;
            String attrName = input.getAttribute("name");
            String attrValue = input.getAttribute("value");
            if (attrName != null && attrValue != null)
            {
                cmccLoginPageFields.put(attrName.trim(), attrValue.trim());
            }
        }
    }

    protected String removeComment(String html)
    {
        String start = "<!--";
        String end = "-->";
        String[] cut1 = html.split(start);
        StringBuffer sb = new StringBuffer();
        for (String temp : cut1)
        {
            int index = temp.indexOf(end);
            if (index >= 0 && index + end.length() < temp.length())
            {
                sb.append(temp.substring(index + end.length()));
            } else
            {
                sb.append(temp);
            }
        }
        return sb.toString();
    }

    protected String extractPortalUrl(String html)
    {
        /**
         * <input type="hidden" name="wlanacname" value="0019.0010.100.00"> <input type="hidden" name="wlanuserip" value="218.205.219.117"> <input type="hidden" name="portalurl"
         * value="http://221.176.1.140/wlan/index.php">
         */
        Parser mHtmlParser = Parser.createParser(html.toLowerCase(), "gb2312");
        NodeClassFilter inputFilter = new NodeClassFilter(InputTag.class);
        try
        {
            NodeList inputList = mHtmlParser.extractAllNodesThatMatch(inputFilter);
            Map<String, String> params = new HashMap<String, String>();
            for (int i = 0; i < inputList.size(); ++i)
            {
                Node tag = inputList.elementAt(i);
                InputTag inputTag = (InputTag) tag;
                // String attrType = inputTag.getAttribute("TYPE");
                String attrName = inputTag.getAttribute("name");
                String attrValue = inputTag.getAttribute("value");
                if (attrName != null && attrValue != null)
                {
                    params.put(attrName.trim(), attrValue.trim());
                }
            }
            if (params.size() > 0)
            {
                String portalUrl = params.get(INDICATOR_REDIRECT_PORTALURL);
                String acname = params.get(INDICATOR_LOGIN_AC_NAME);
                String userip = params.get(INDICATOR_LOGIN_USER_IP);
                if (portalUrl != null && portalUrl.length() > 0 && acname != null && acname.length() > 0 && userip != null && userip.length() > 0)
                {
                    StringBuffer location = new StringBuffer(portalUrl);
                    location.append("?");
                    location.append(INDICATOR_LOGIN_AC_NAME).append("=").append(acname);
                    location.append("&");
                    location.append(INDICATOR_LOGIN_USER_IP).append("=").append(userip);
                    return location.toString();
                }
            }
            return null;
        } catch (ParserException e)
        {
            return null;
        }
    }

    protected String extractHref(String html)
    {
        /**
         * <script language="javascript"> window.location.href="http://221.176.1.140/wlan/index.php?wlanacname=1037.0010.100.00&wlanuserip=117.134.26.92&ssid=&vlan=4095"; var
         * PORTALLOGINURL="https://221.176.1.140/wlan/bin/login.pl"; var PORTALLOGOUTURL="https://221.176.1.140/wlan/bin/logout.pl"; </script>
         */
        String PREFIX = "window.location.href";
        int index = html.indexOf(PREFIX);
        if (index != -1)
        {
            String temp = html.substring(index + PREFIX.length());
            index = temp.indexOf("=");
            if (index != -1 && index < temp.length() - 1)
            {
                temp = temp.substring(index + 1).trim();
                if (temp.length() > 0)
                {
                    int start = 0;
                    int end = 1;
                    if (temp.startsWith("\""))
                    {
                        start = 1;
                        end = temp.indexOf("\"", start);
                    } else
                    {
                        end = temp.indexOf(";", start);
                    }
                    if (end > start)
                    {
                        return temp.substring(start, end);
                    }
                }
            }
        }
        return null;
    }

    protected String extractNextUrl(String html)
    {
        /**
         * redirect html sample: <HTML> <HEAD> <META HTTP-EQUIV="REFRESH" CONTENT="1;URL=http://117.134.32.234   "> <TITLE> </TITLE> </HEAD> <?xml version="1.0" encoding="UTF-8"?>
         * <WISPAccessGatewayParam xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://www.acmewisp.com/WISPAccessGatewayParam.xsd"> <Proxy>
         * <MessageType>110</MessageType> <NextURL>http://221.176.1.140/wlan/index.php?wlanacip=117.134.32.234&wlanuserip=117.133.202.211&wlanacname=0099.0010.100.00</NextURL>
         * <ResponseCode>200</ResponseCode> </Proxy> </WISPAccessGatewayParam> </HTML>
         */
        int startTagNextURL = html.toLowerCase().indexOf("<nexturl>");
        int endTagNextURL = html.toLowerCase().indexOf("</nexturl>");

        // extract "NextURL" from temp HTML and re-direct
        if (startTagNextURL != -1 && endTagNextURL != -1)
        {
            return html.substring(startTagNextURL + "<nexturl>".length(), endTagNextURL);
        }
        return null;
    }

    protected String doLogin() throws IOException, ParserException
    {
        String action = cmccLoginUrl;
        if (action == null || action.trim().length() == 0)
            action = CMCC_PORTAL_URL;
        if (userName == null || password == null)
            throw new IllegalStateException("userName or password can not be null.");
        cmccLoginPageFields.put(INDICATOR_LOGIN_USERNAME, userName);
        cmccLoginPageFields.put(INDICATOR_LOGIN_PASSWORD, password);
        cmccLoginPageFields.put(INDICATOR_LOGIN_FORCEFLAG, "1");
        HttpResponseResult result = doHttpPost(action, cmccLoginPageFields);
        String html = result.getDataString("gb2312");
        String keywordLoginRes = KEYWORD_CMCCCS + SEPARATOR + KEYWORD_LOGINRES + SEPARATOR;
        int keywordIndex = html.indexOf(keywordLoginRes);
        if (keywordIndex == -1)
            throw new ParserException("can not find keyword from login response.");
        String subHtml = html.substring(keywordIndex + keywordLoginRes.length());
        int start = subHtml.indexOf(SEPARATOR);
        if (start == -1)
            throw new ParserException("can not find the begin separator from login response.");
        String sign = subHtml.substring(0, start);
        if (!"0".equals(sign))
        {
            subHtml = subHtml.substring(start + SEPARATOR.length());
            int end = subHtml.indexOf(SEPARATOR);
            if (end == -1)
                throw new ParserException("can not find the end separator from login response.");
            return subHtml.substring(0, end);
        }
        try
        {
            // when it is login response html, extract the parameters
            doParseLoginPage(html);
        } catch (Exception e)
        {
            LogManager.logE(User.class, "deal login response failed.", e);
        }
        return null;
    }

    public void cancelLogin()
    {
        // TODO Auto-generated method stub
        isCancelLogin = true;
    }

    public boolean redirectedToPortal() throws IOException
    {
        HttpResponseResult result = doHttpGet("http://www.baidu.com");
        String html = result.getDataString("gb2312");
        if (html.indexOf(KEYWORD_CMCCCS + SEPARATOR + KEYWORD_LOGINREQ) >= 0)
        {
            this.cmccPortalHtml = html;
            return true;
        }
        return false;
    }

    protected HttpResponseResult doHttpGet(String url) throws IOException
    {
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        values.add("gb2312");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_ACCEPT_CHARSET, values);
        values = new ArrayList<String>();
        values.add("G3WLAN");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_USER_AGENT, values);
        return HttpConnectionManager.doGet(url, "gb2312", true, 15000, requestHeaders);
    }

    protected HttpResponseResult doHttpPost(String url, Map<String, String> params) throws IOException
    {
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        values.add("gb2312");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_ACCEPT_CHARSET, values);
        values = new ArrayList<String>();
        values.add("G3WLAN");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_USER_AGENT, values);
        return HttpConnectionManager.doPost(url, "gb2312", true, 15000, requestHeaders, params, "gb2312");
    }

    /**
     * <p>返回null表示成功，否则返回失败消息
     * 
     * @return
     * @throws IOException
     * @throws ParserException
     */
    public String logout() throws IOException, ParserException
    {
        // TODO Auto-generated method stub
        String action = cmccLoginUrl;
        if (action == null || action.trim().length() == 0)
            action = CMCC_PORTAL_URL;
        HttpResponseResult result = doHttpPost(action, cmccLoginPageFields);
        String html = result.getDataString("gb2312");
        String keywordLogoutRes = KEYWORD_CMCCCS + SEPARATOR + KEYWORD_OFFLINERES + SEPARATOR;
        int keywordIndex = html.indexOf(keywordLogoutRes);
        if (keywordIndex == -1)
            throw new ParserException("can not find keyword from logout response.");
        String subHtml = html.substring(keywordIndex + keywordLogoutRes.length());
        int start = subHtml.indexOf(SEPARATOR);
        if (start == -1)
            throw new ParserException("can not find the begin separator from logout response.");
        String sign = subHtml.substring(0, start);
        if (!"0".equals(sign))
        {
            subHtml = subHtml.substring(start + SEPARATOR.length());
            int end = subHtml.indexOf(SEPARATOR);
            if (end == -1)
                throw new ParserException("can not find the end separator from logout response.");
            return subHtml.substring(0, end);
        }
        return null;
    }

    protected class FormFilter extends NodeClassFilter
    {
        private static final long serialVersionUID = 1L;
        protected String          formName         = null;

        public FormFilter(String formName)
        {
            super(FormTag.class);
            this.formName = formName;
        }

        @Override
        public boolean accept(Node node)
        {
            if (super.accept(node))
            {
                if (node instanceof FormTag)
                {
                    FormTag form = (FormTag) node;
                    if (formName != null & formName.equals(form.getFormName()))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }

}
