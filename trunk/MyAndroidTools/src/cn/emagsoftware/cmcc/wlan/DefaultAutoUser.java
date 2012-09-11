package cn.emagsoftware.cmcc.wlan;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import android.content.Context;

import cn.emagsoftware.net.http.HtmlManager;
import cn.emagsoftware.net.http.HttpConnectionManager;
import cn.emagsoftware.net.http.HttpResponseResult;
import cn.emagsoftware.util.LogManager;

class DefaultAutoUser extends AutoUser
{

    protected static final String GUIDE_URL                    = "http://www.baidu.com";
    protected static final String GUIDE_HOST                   = "www.baidu.com";
    protected static final String GD_JSESSIONID                = "JSESSIONID=";
    protected static final String BJ_PHPSESSID                 = "PHPSESSID=";
    protected static final String KEYWORD_CMCCCS               = "cmcccs";
    protected static final String KEYWORD_LOGINREQ             = "login_req";
    protected static final String KEYWORD_LOGINRES             = "login_res";
    protected static final String KEYWORD_APPLYPWDRES          = "applypwd_res";
    protected static final String SEPARATOR                    = "|";
    protected static final String CMCC_PORTAL_URL              = "https://221.176.1.140/wlan/index.php";
    // for redirection
    protected static final String INDICATOR_REDIRECT_PORTALURL = "portalurl";
    protected static final String INDICATOR_LOGIN_AC_NAME      = "wlanacname";
    protected static final String INDICATOR_LOGIN_USER_IP      = "wlanuserip";
    // form parameters in cmcc logining page
    protected static final String CMCC_LOGINFORM_NAME          = "loginform";
    protected static final String CMCC_LOGOUTFORM_NAME         = "portal";

    protected Context             context                      = null;
    protected boolean             isCancelLogin                = false;
    protected String              sessionCookie                = null;
    protected String              cmccPortalUrl                = null;
    protected String              cmccPageUrl                  = null;
    protected String              cmccPageHtml                 = null;
    protected String              cmccLoginPageHtml            = null;
    protected Map<String, String> cmccLogoutPageFields         = new HashMap<String, String>();
    protected String              cmccLogoutUrl                = null;

    public DefaultAutoUser(Context context)
    {
        super();
        if (context == null)
            throw new NullPointerException();
        this.context = context;
    }

    @Override
    public String requestPassword()
    {
        // TODO Auto-generated method stub
        if (super.userName == null)
            return context.getString(context.getResources().getIdentifier("DefaultAutoUser_username_empty", "string", context.getPackageName()));
        try
        {
            boolean isLogged = isLogged();
            if (isLogged)
                return context.getString(context.getResources().getIdentifier("DefaultAutoUser_requestpwd_alreadylogin", "string", context.getPackageName()));
            parseLoginPage(this.cmccPageHtml);
            String action = cmccPortalUrl;
            cmccPortalUrl = null;
            if (action == null || action.trim().length() == 0)
                action = CMCC_PORTAL_URL;
            Map<String, String> pageFields = new HashMap<String, String>();
            pageFields.put("USER", super.userName);
            pageFields.put("actiontype", "APPLYPWD");
            HttpResponseResult result = doHttpPostContainsRedirect(action, pageFields);
            String html = result.getDataString("gb2312");
            String keywordPwdRes = KEYWORD_CMCCCS + SEPARATOR + KEYWORD_APPLYPWDRES + SEPARATOR;
            int start = html.indexOf(keywordPwdRes);
            if (start == -1)
                throw new ParserException("can not find keyword from password response.");
            start = start + keywordPwdRes.length();
            start = html.indexOf(SEPARATOR, start);
            if (start == -1)
                throw new ParserException("can not find the begin separator from password response.");
            start = start + 1;
            String temp = html.substring(start);
            int end = temp.indexOf(SEPARATOR);
            if (end == -1)
                throw new ParserException("can not find the end separator from password response.");
            return temp.substring(0, end);
        } catch (IOException e)
        {
            LogManager.logE(DefaultAutoUser.class, "requestPassword failed.", e);
            return context.getString(context.getResources().getIdentifier("DefaultAutoUser_net_error", "string", context.getPackageName()));
        } catch (ParserException e)
        {
            LogManager.logE(DefaultAutoUser.class, "requestPassword failed.", e);
            return context.getString(context.getResources().getIdentifier("DefaultAutoUser_parse_error", "string", context.getPackageName()));
        }
    }

    protected void parseLoginPage(String pageHtml) throws ParserException, IOException
    {
        String keywordLoginReq = KEYWORD_CMCCCS + SEPARATOR + KEYWORD_LOGINREQ;
        if (keywordLoginReq != null && pageHtml.indexOf(keywordLoginReq) != -1)
        { // 当前页面已经是CMCC登录页面
            doParseLoginPage(pageHtml);
        } else
        {
            String formatHtml = HtmlManager.removeComment(pageHtml);
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
            HttpResponseResult result = doHttpGetContainsRedirect(location);
            parseLoginPage(result.getDataString("gb2312"));
        }
    }

    protected void doParseLoginPage(String loginPageHtml) throws ParserException
    {
        String formatHtml = HtmlManager.removeComment(loginPageHtml);
        Parser mHtmlParser = Parser.createParser(formatHtml, "gb2312");
        FormFilter filter = new FormFilter(CMCC_LOGINFORM_NAME);
        NodeList formList = mHtmlParser.parse(filter);
        if (formList == null || formList.size() == 0)
            throw new ParserException("could not find the form named '" + CMCC_LOGINFORM_NAME + "'");
        Node tag = formList.elementAt(0);
        FormTag formTag = (FormTag) tag;
        // 获取提交表单的URL
        String formAction = formTag.getFormLocation();
        if (formAction != null && formAction.trim().length() > 0)
        {
            this.cmccPortalUrl = formAction.trim();
        }
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

    @Override
    public String login()
    {
        // TODO Auto-generated method stub
        isCancelLogin = false;
        if (isCancelLogin)
            return context.getString(context.getResources().getIdentifier("DefaultAutoUser_login_cancel", "string", context.getPackageName()));
        if (super.userName == null)
            return context.getString(context.getResources().getIdentifier("DefaultAutoUser_username_empty", "string", context.getPackageName()));
        if (super.password == null)
            return context.getString(context.getResources().getIdentifier("DefaultAutoUser_password_empty", "string", context.getPackageName()));
        try
        {
            boolean isLogged = isLogged();
            if (isLogged)
                return null; // 已经登录，将直接返回null表示登录成功
            if (isCancelLogin)
                return context.getString(context.getResources().getIdentifier("DefaultAutoUser_login_cancel", "string", context.getPackageName()));
            Parser mHtmlParser = Parser.createParser(cmccPageHtml.toLowerCase(), "gb2312");
            NodeClassFilter frameFilter = new NodeClassFilter(FrameTag.class);
            NodeList nl = mHtmlParser.parse(frameFilter);
            if (nl == null || nl.size() == 0)
                throw new ParserException();
            FrameTag ft = (FrameTag) nl.elementAt(0);
            String loginUrl = ft.getAttribute("src");
            if (loginUrl == null || loginUrl.equals(""))
                throw new ParserException();
            this.cmccLoginPageHtml = doHttpGetContainsRedirect(loginUrl).getDataString("gb2312");
            if (isCancelLogin)
                return context.getString(context.getResources().getIdentifier("DefaultAutoUser_login_cancel", "string", context.getPackageName()));
            mHtmlParser = Parser.createParser(cmccLoginPageHtml.toLowerCase(), "gb2312");
            FormFilter filter = new FormFilter("autologin");
            NodeList formList = mHtmlParser.parse(filter);
            if (formList == null || formList.size() == 0)
                throw new ParserException();
            FormTag formTag = (FormTag) formList.elementAt(0);
            String submitUrl = formTag.getFormLocation();
            if (submitUrl == null || submitUrl.equals(""))
                throw new ParserException();
            // 获取表单元素
            Map<String, String> params = new HashMap<String, String>();
            NodeList inputTags = formTag.getFormInputs();
            for (int j = 0; j < inputTags.size(); j++)
            {
                Node node = inputTags.elementAt(j);
                InputTag input = (InputTag) node;
                String attrName = input.getAttribute("name");
                String attrValue = input.getAttribute("value");
                if (attrName != null && attrValue != null)
                {
                    params.put(attrName.trim(), attrValue.trim());
                }
            }
            params.put("autousername", super.userName);
            params.put("autopassword", super.password);
            if (isCancelLogin)
                return context.getString(context.getResources().getIdentifier("DefaultAutoUser_login_cancel", "string", context.getPackageName()));
            String loginResult = doHttpPostContainsRedirect(submitUrl, params).getDataString("gb2312");
            return parseLoginResult(loginResult);
        } catch (IOException e)
        {
            LogManager.logE(DefaultAutoUser.class, "logining failed.", e);
            return context.getString(context.getResources().getIdentifier("DefaultAutoUser_net_error", "string", context.getPackageName()));
        } catch (ParserException e)
        {
            LogManager.logE(DefaultAutoUser.class, "logining failed.", e);
            return context.getString(context.getResources().getIdentifier("DefaultAutoUser_parse_error", "string", context.getPackageName()));
        }
    }

    protected String parseLoginResult(String loginResult) throws ParserException, IOException
    {
        int alertIndex = loginResult.indexOf("alert");
        int confirmIndex = loginResult.indexOf("confirm");
        if (alertIndex == -1 && confirmIndex == -1)
        { // 登录成功
            /*
             * try{ //获取表单参数，为下线提供条件 Parser mParser = Parser.createParser(loginResult.toLowerCase(), "gb2312"); NodeClassFilter fFilter = new NodeClassFilter(FrameTag.class); NodeList nodeL =
             * mParser.parse(fFilter); if(nodeL == null || nodeL.size() == 0) throw new ParserException(); FrameTag fTag = (FrameTag)nodeL.elementAt(0); String frameUrl = fTag.getAttribute("src");
             * if(frameUrl == null || frameUrl.equals("")) throw new ParserException(); String offLinePage = doHttpGetContainsRedirect(frameUrl).getDataString("gb2312"); mParser =
             * Parser.createParser(offLinePage, "gb2312"); FormFilter formFilter = new FormFilter(CMCC_LOGOUTFORM_NAME); NodeList formLi = mParser.parse(formFilter); if(formLi == null || formLi.size()
             * == 0) throw new ParserException("could not find the form named '"+CMCC_LOGOUTFORM_NAME+"'"); Node tag = formLi.elementAt(0); FormTag form = (FormTag) tag; cmccLogoutPageFields.clear();
             * //获取提交表单的URL String formAction = form.getFormLocation(); if(formAction == null || formAction.trim().length() == 0){ int index = offLinePage.indexOf("thisform.action"); if(index == -1)
             * throw new ParserException(); int beginAction = offLinePage.indexOf("\'", index); if(beginAction == -1){ beginAction = offLinePage.indexOf("\"", index); if(beginAction == -1) throw new
             * ParserException(); } int endAction = offLinePage.indexOf("\'", beginAction + 1); if(endAction == -1){ endAction = offLinePage.indexOf("\"", beginAction + 1); if(endAction == -1) throw
             * new ParserException(); } String action = offLinePage.substring(beginAction + 1, endAction).trim(); if(action.toLowerCase().startsWith("http")){ formAction = action; }else{ int urlIndex
             * = frameUrl.lastIndexOf("/"); if(urlIndex == -1) throw new ParserException(); formAction = frameUrl.substring(0, urlIndex) + "/" + action; } } cmccLogoutUrl = formAction.trim(); //获取表单元素
             * NodeList inputs = form.getFormInputs(); for (int j = 0; j < inputs.size(); j++) { Node node = inputs.elementAt(j); InputTag input = (InputTag) node; String type =
             * input.getAttribute("type"); if("checkbox".equalsIgnoreCase(type) || "button".equalsIgnoreCase(type)) continue; String attrName = input.getAttribute("name"); String attrValue =
             * input.getAttribute("value"); if(attrName != null && attrValue != null) { cmccLogoutPageFields.put(attrName.trim(), attrValue.trim()); } } //暂时直接写死该项的值，以后将作修改
             * cmccLogoutPageFields.put("logouttype", "TYPESUBMIT"); }catch(ParserException e){ LogManager.logE(DefaultAutoUser.class, "deal logining result page failed.", e); }catch(IOException e){
             * LogManager.logE(DefaultAutoUser.class, "deal logining result page failed.", e); }catch(RuntimeException e){ LogManager.logE(DefaultAutoUser.class, "deal logining result page failed.",
             * e); }
             */
            return null;
        } else if (confirmIndex != -1)
        { // 当前登录的用户已在线
            int locationIndex = loginResult.indexOf("window.location", confirmIndex);
            if (locationIndex == -1)
                throw new ParserException();
            int beginIndex = loginResult.indexOf("\'", locationIndex);
            if (beginIndex == -1)
            {
                beginIndex = loginResult.indexOf("\"", locationIndex);
                if (beginIndex == -1)
                    throw new ParserException();
            }
            int endIndex = loginResult.indexOf("\'", beginIndex + 1);
            if (endIndex == -1)
            {
                endIndex = loginResult.indexOf("\"", beginIndex + 1);
                if (endIndex == -1)
                    throw new ParserException();
            }
            String secondLoginUrl = loginResult.substring(beginIndex + 1, endIndex);
            String secondLoginResult = doHttpGetContainsRedirect(secondLoginUrl).getDataString("gb2312");
            return parseLoginResult(secondLoginResult);
        } else
        { // 登录失败
            int begin = loginResult.indexOf("\"", alertIndex);
            if (begin == -1)
            {
                begin = loginResult.indexOf("\'", alertIndex);
                if (begin == -1)
                    throw new ParserException();
            }
            int end = loginResult.indexOf("\"", begin + 1);
            if (end == -1)
            {
                end = loginResult.indexOf("\'", begin + 1);
                if (end == -1)
                    throw new ParserException();
            }
            return loginResult.substring(begin + 1, end);
        }
    }

    @Override
    public void cancelLogin()
    {
        // TODO Auto-generated method stub
        isCancelLogin = true;
    }

    @Override
    public boolean isLogged() throws IOException
    {
        // TODO Auto-generated method stub
        HttpResponseResult result = doHttpGetContainsRedirect(GUIDE_URL);
        URL url = result.getResponseURL();
        String host = url.getHost();
        String html = result.getDataString("gb2312");
        if (GUIDE_HOST.equalsIgnoreCase(host) && html.indexOf(GUIDE_HOST) >= 0)
        { // 若能访问到原始站点，证明已登录
            return true;
        }
        // 若不能访问原始站点，即重定向到了CMCC页面
        this.cmccPageUrl = url.toString();
        this.cmccPageHtml = html;
        return false;
    }

    protected HttpResponseResult doHttpGetContainsRedirect(String url) throws IOException
    {
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        values.add("gb2312");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_ACCEPT_CHARSET, values);
        values = new ArrayList<String>();
        values.add("application/x-www-form-urlencoded");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_CONTENT_TYPE, values);
        values = new ArrayList<String>();
        values.add("G3WLAN");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_USER_AGENT, values);
        if (sessionCookie != null)
        {
            values = new ArrayList<String>();
            values.add(sessionCookie);
            requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_COOKIE, values);
        }
        HttpResponseResult result = HttpConnectionManager.doGet(url, "gb2312", false, 15000, requestHeaders);
        int code = result.getResponseCode();
        while (code != HttpURLConnection.HTTP_OK && code == HttpURLConnection.HTTP_MOVED_TEMP)
        {
            List<String> headerValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_LOCATION.toLowerCase());
            String location = headerValues.get(0);
            result = HttpConnectionManager.doGet(location, "gb2312", false, 15000, requestHeaders);
            code = result.getResponseCode();
        }
        if (code != HttpURLConnection.HTTP_OK)
            throw new IOException("requesting url returns code:" + code);
        // 以下获取cookie
        List<String> setCookieValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_SET_COOKIE.toLowerCase());
        if (setCookieValues != null && setCookieValues.size() > 0)
        {
            String setCookieValue = setCookieValues.get(0);
            if (setCookieValue != null)
            {
                String[] setCookieGroup = setCookieValue.split(";");
                for (String tmp : setCookieGroup)
                {
                    if (tmp.trim().startsWith(GD_JSESSIONID) // for Guangdong: "JSESSIONID="
                            || tmp.trim().startsWith(BJ_PHPSESSID) // for Beijing: "PHPSESSID="
                    )
                    {
                        this.sessionCookie = tmp.trim();
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected HttpResponseResult doHttpPostContainsRedirect(String url, Map<String, String> params) throws IOException
    {
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        values.add("gb2312");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_ACCEPT_CHARSET, values);
        values = new ArrayList<String>();
        values.add("application/x-www-form-urlencoded");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_CONTENT_TYPE, values);
        values = new ArrayList<String>();
        values.add("G3WLAN");
        requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_USER_AGENT, values);
        if (sessionCookie != null)
        {
            values = new ArrayList<String>();
            values.add(sessionCookie);
            requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_COOKIE, values);
        }
        HttpResponseResult result = HttpConnectionManager.doPost(url, "gb2312", false, 15000, requestHeaders, params, "gb2312");
        int code = result.getResponseCode();
        while (code != HttpURLConnection.HTTP_OK && code == HttpURLConnection.HTTP_MOVED_TEMP)
        {
            List<String> headerValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_LOCATION.toLowerCase());
            String location = headerValues.get(0);
            result = HttpConnectionManager.doGet(location, "gb2312", false, 15000, requestHeaders);
            code = result.getResponseCode();
        }
        if (code != HttpURLConnection.HTTP_OK)
            throw new IOException("requesting url returns code:" + code);
        // 以下获取cookie
        List<String> setCookieValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_SET_COOKIE.toLowerCase());
        if (setCookieValues != null && setCookieValues.size() > 0)
        {
            String setCookieValue = setCookieValues.get(0);
            if (setCookieValue != null)
            {
                String[] setCookieGroup = setCookieValue.split(";");
                for (String tmp : setCookieGroup)
                {
                    if (tmp.trim().startsWith(GD_JSESSIONID) // for Guangdong: "JSESSIONID="
                            || tmp.trim().startsWith(BJ_PHPSESSID) // for Beijing: "PHPSESSID="
                    )
                    {
                        this.sessionCookie = tmp.trim();
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String logout()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("this method has not supported yet.");
        /*
         * String action = cmccLogoutUrl; if(action == null) return context.getString(context.getResources().getIdentifier("DefaultAutoUser_logout_failure", "string", context.getPackageName())); try{
         * HttpResponseResult result = doHttpPostContainsRedirect(action, cmccLogoutPageFields); String html = result.getDataString("gb2312");
         * 
         * LogManager.logI(DefaultAutoUser.class, html);
         * 
         * return null; }catch(IOException e){ LogManager.logE(DefaultAutoUser.class, "logouting failed.", e); return
         * context.getString(context.getResources().getIdentifier("DefaultAutoUser_net_error", "string", context.getPackageName())); }
         */
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
