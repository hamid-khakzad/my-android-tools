package cn.emagsoftware.net.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import cn.emagsoftware.util.Base64;
import cn.emagsoftware.util.LogManager;

/**
 * Http Connection Manager
 * 
 * @author Wendell
 * @version 3.8
 */
public final class HttpConnectionManager
{

    public static final String               HTTPS_PREFIX                   = "https";

    public static final String               HEADER_REQUEST_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String               HEADER_REQUEST_CONNECTION      = "Connection";
    public static final String               HEADER_REQUEST_CACHE_CONTROL   = "Cache-Control";
    public static final String               HEADER_REQUEST_ACCEPT_CHARSET  = "Accept-Charset";
    public static final String               HEADER_REQUEST_CONTENT_TYPE    = "Content-Type";
    public static final String               HEADER_REQUEST_USER_AGENT      = "User-Agent";
    public static final String               HEADER_REQUEST_COOKIE          = "Cookie";

    public static final String               HEADER_RESPONSE_CONTENT_TYPE   = "Content-Type";
    public static final String               HEADER_RESPONSE_LOCATION       = "Location";
    public static final String               HEADER_RESPONSE_SET_COOKIE     = "Set-Cookie";

    public static final int                  REDIRECT_MAX_COUNT             = 10;
    public static final int                  CMWAP_CHARGEPAGE_MAX_COUNT     = 5;

    private static boolean                   isKeepSession                  = false;
    private static Map<String, List<String>> sessions                       = Collections.synchronizedMap(new HashMap<String, List<String>>());

    private static boolean                   isUseCMWap                     = false;

    private HttpConnectionManager()
    {
    }

    public static void setKeepSession(boolean isKeepSession)
    {
        HttpConnectionManager.isKeepSession = isKeepSession;
    }

    public static void clearSessions()
    {
        sessions.clear();
    }

    /**
     * <p>�����Ƿ�ʹ�����й��ƶ�CMWap����
     * 
     * @param isUseCMWap
     */
    public static void setUseCMWap(boolean isUseCMWap)
    {
        HttpConnectionManager.isUseCMWap = isUseCMWap;
    }

    /**
     * <p>��ȡ�Ƿ�ʹ�����й��ƶ�CMWap����
     * 
     * @return
     */
    public static boolean isUseCMWap()
    {
        return isUseCMWap;
    }

    /**
     * ����http get����
     * 
     * @param url �����url
     * @param urlEnc ��url����URL������ַ���������ҪURL����ʱ�ɴ�null
     * @param followRedirects �Ƿ��Զ��ض���
     * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
     * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
     * @return HttpResponseResultStreamʵ��
     * @throws IOException
     */
    public static HttpResponseResultStream doGetForStream(String url, String urlEnc, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders) throws IOException
    {
        HttpURLConnection httpConn = null;
        InputStream input = null;
        try
        {
            httpConn = openConnection(url, urlEnc, "GET", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, null);
            HttpResponseResultStream result = new HttpResponseResultStream();
            result.setResponseURL(httpConn.getURL());
            int rspCode = httpConn.getResponseCode();
            result.setResponseCode(rspCode);
            result.setResponseHeaders(httpConn.getHeaderFields());
            input = httpConn.getInputStream();
            result.setResultStream(input);
            result.setHttpURLConn(httpConn);
            return result;
        } catch (IOException e)
        {
            try
            {
                if (input != null)
                    input.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
            throw e;
        }
    }

    public static HttpResponseResult doGet(String url, String urlEnc, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders) throws IOException
    {
        HttpResponseResultStream result = doGetForStream(url, urlEnc, followRedirects, connOrReadTimeout, requestHeaders);
        result.generateData();
        return result;
    }

    /**
     * ����http post���󣬽���ֵΪapplication/x-www-form-urlencoded��Content-Type���ύ��ֵ�Բ���
     * 
     * @param url �����url
     * @param urlEnc ��url����URL������ַ���������ҪURL����ʱ�ɴ�null
     * @param followRedirects �Ƿ��Զ��ض���
     * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
     * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
     * @param postParams �ύ��POST����������Ҫʱ�ɴ�null
     * @param postParamsEnc ���ύ��POST��������URL������ַ���������ҪURL����ʱ�ɴ�null
     * @return HttpResponseResultStreamʵ��
     * @throws IOException
     */
    public static HttpResponseResultStream doPostForStream(String url, String urlEnc, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders,
            Map<String, String> postParams, String postParamsEnc) throws IOException
    {
        HttpURLConnection httpConn = null;
        InputStream input = null;
        try
        {
            if (requestHeaders == null)
                requestHeaders = new HashMap<String, List<String>>();
            List<String> contentTypes = requestHeaders.get(HEADER_REQUEST_CONTENT_TYPE);
            if (contentTypes == null)
                contentTypes = new ArrayList<String>();
            contentTypes.add("application/x-www-form-urlencoded");
            requestHeaders.put(HEADER_REQUEST_CONTENT_TYPE, contentTypes);
            byte[] paramsData = null;
            if (postParams != null)
            {
                String postParamsStr = HttpManager.concatParams(postParams);
                if (postParamsEnc != null)
                    postParamsStr = HttpManager.encodeParams(postParamsStr, postParamsEnc); // post����ʱ����Բ�������URL����
                paramsData = postParamsStr.getBytes(); // �����ⲿ���ڲ�URL����֮��Ĳ���ֻ��Ӣ�ģ����������ַ������б���
            }
            httpConn = openConnection(url, urlEnc, "POST", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, paramsData);
            HttpResponseResultStream result = new HttpResponseResultStream();
            result.setResponseURL(httpConn.getURL());
            int rspCode = httpConn.getResponseCode();
            result.setResponseCode(rspCode);
            result.setResponseHeaders(httpConn.getHeaderFields());
            input = httpConn.getInputStream();
            result.setResultStream(input);
            result.setHttpURLConn(httpConn);
            return result;
        } catch (IOException e)
        {
            try
            {
                if (input != null)
                    input.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
            throw e;
        }
    }

    public static HttpResponseResult doPost(String url, String urlEnc, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, Map<String, String> postParams,
            String postParamsEnc) throws IOException
    {
        HttpResponseResultStream result = doPostForStream(url, urlEnc, followRedirects, connOrReadTimeout, requestHeaders, postParams, postParamsEnc);
        result.generateData();
        return result;
    }

    /**
     * ����http post���󣬽���ֵΪapplication/octet-stream��Content-Type���ύ����
     * 
     * @param url �����url
     * @param urlEnc ��url����URL������ַ���������ҪURL����ʱ�ɴ�null
     * @param followRedirects �Ƿ��Զ��ض���
     * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
     * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
     * @param postData �ύ��POST���ݣ�����Ҫʱ�ɴ�null
     * @return HttpResponseResultStreamʵ��
     * @throws IOException
     */
    public static HttpResponseResultStream doPostForStream(String url, String urlEnc, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, byte[] postData)
            throws IOException
    {
        HttpURLConnection httpConn = null;
        InputStream input = null;
        try
        {
            if (requestHeaders == null)
                requestHeaders = new HashMap<String, List<String>>();
            List<String> contentTypes = requestHeaders.get(HEADER_REQUEST_CONTENT_TYPE);
            if (contentTypes == null)
                contentTypes = new ArrayList<String>();
            contentTypes.add("application/octet-stream");
            requestHeaders.put(HEADER_REQUEST_CONTENT_TYPE, contentTypes);
            if (postData == null)
                postData = new byte[] {}; // ò�����application/octet-stream�����������������������׳�FileNotFoundException���������Android�ĵײ�ʵ����ȱ��
            httpConn = openConnection(url, urlEnc, "POST", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, postData);
            HttpResponseResultStream result = new HttpResponseResultStream();
            result.setResponseURL(httpConn.getURL());
            int rspCode = httpConn.getResponseCode();
            result.setResponseCode(rspCode);
            result.setResponseHeaders(httpConn.getHeaderFields());
            input = httpConn.getInputStream();
            result.setResultStream(input);
            result.setHttpURLConn(httpConn);
            return result;
        } catch (IOException e)
        {
            try
            {
                if (input != null)
                    input.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
            throw e;
        }
    }

    public static HttpResponseResult doPost(String url, String urlEnc, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, byte[] postData) throws IOException
    {
        HttpResponseResultStream result = doPostForStream(url, urlEnc, followRedirects, connOrReadTimeout, requestHeaders, postData);
        result.generateData();
        return result;
    }

    /**
     * ����HttpURLConnectionʵ��
     * 
     * @param url �����url
     * @param urlEnc ��url����URL������ַ���������ҪURL����ʱ�ɴ�null
     * @param method ����ķ�ʽ����GET,POST
     * @param followRedirects �Ƿ��Զ��ض���
     * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
     * @param currentRedirectCount ��ǰ�ǵڼ����ض���
     * @param currentCMWapChargePageCount ��ǰ�ǵڼ��γ���CMWap�ʷ���ʾҳ��
     * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
     * @param postData methodΪPOSTʱ�ύ�����ݣ�����Ҫʱ�ɴ�null
     * @return HttpURLConnectionʵ��
     * @throws IOException
     */
    private static HttpURLConnection openConnection(String url, String urlEnc, String method, boolean followRedirects, int connOrReadTimeout, int currentRedirectCount,
            int currentCMWapChargePageCount, Map<String, List<String>> requestHeaders, byte[] postData) throws IOException
    {
        if (currentRedirectCount < 0)
            throw new IllegalArgumentException("current redirect count can not set to below zero");
        if (currentRedirectCount > REDIRECT_MAX_COUNT)
            throw new IOException("too many redirect times");
        if (currentCMWapChargePageCount < 0)
            throw new IllegalArgumentException("current CMWap charge page count can not set to below zero");
        if (currentCMWapChargePageCount > CMWAP_CHARGEPAGE_MAX_COUNT)
            throw new IOException("too many showing CMWap charge page times");
        String packUrl = null;
        if (urlEnc == null)
            packUrl = url;
        else
            packUrl = HttpManager.encodeURL(url, urlEnc);
        URL originalUrl = new URL(packUrl);
        URL myUrl = originalUrl;
        String prefix = null;
        if (isUseCMWap())
        {
            prefix = myUrl.getAuthority();
            String myUrlStr = "http://10.0.0.172".concat(myUrl.getPath());
            String query = myUrl.getQuery();
            if (query != null)
                myUrlStr = myUrlStr.concat("?").concat(query);
            myUrl = new URL(myUrlStr);
        }
        boolean isSSL = myUrl.toString().toLowerCase().startsWith(HTTPS_PREFIX);
        HttpURLConnection httpConn = null;
        OutputStream output = null;
        try
        {
            LogManager.logI(HttpConnectionManager.class, "request url '".concat(myUrl.toString()).concat("'..."));
            if (isSSL)
            {
                SSLContext sslCont = SSLContext.getInstance("TLS");
                sslCont.init(null, new TrustManager[] { new MyX509TrustManager() }, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslCont.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier(myUrl.getHost()));
            }
            httpConn = (HttpsURLConnection) myUrl.openConnection();
            httpConn.setRequestMethod(method);
            HttpURLConnection.setFollowRedirects(false);
            httpConn.setInstanceFollowRedirects(false);
            httpConn.setDoInput(true);
            if (method.equalsIgnoreCase("POST"))
                httpConn.setDoOutput(true); // �����ԣ���Android 4.0��ĳЩ����ķ�����ʵ���£��������setDoOutput(true)�����յ�405��http״̬��
            else
                httpConn.setDoOutput(false); // �����ԣ���Android 4.0��ĳЩ����ķ�����ʵ���£����������setDoOutput(false)�����յ�405��http״̬��
            httpConn.setReadTimeout(connOrReadTimeout);
            httpConn.setConnectTimeout(connOrReadTimeout);
            if (isKeepSession)
            {
                String session = querySession(originalUrl); // ��Ҫʹ��ԭʼURL��ѯsession
                if (session != null)
                {
                    LogManager.logI(HttpConnectionManager.class, "queried session(" + session + ") for url " + myUrl);
                    httpConn.addRequestProperty(HEADER_REQUEST_COOKIE, session);
                }
            }
            if (isUseCMWap())
            {
                httpConn.addRequestProperty("X-Online-Host", prefix);
            }
            if (requestHeaders != null)
            {
                Iterator<String> keys = requestHeaders.keySet().iterator();
                while (keys.hasNext())
                {
                    String key = keys.next();
                    List<String> values = requestHeaders.get(key);
                    for (String value : values)
                    {
                        httpConn.addRequestProperty(key, value);
                    }
                }
            }
            if (method.equalsIgnoreCase("POST") && postData != null)
            {
                output = httpConn.getOutputStream();
                BufferedOutputStream buffOutput = new BufferedOutputStream(output);
                buffOutput.write(postData);
                buffOutput.flush();
                output.close();
            }
            int rspCode = httpConn.getResponseCode();
            if (rspCode == HttpURLConnection.HTTP_OK || rspCode == HttpURLConnection.HTTP_PARTIAL)
            {
                if (isUseCMWap())
                {
                    String contentType = httpConn.getHeaderField(HEADER_RESPONSE_CONTENT_TYPE);
                    if (contentType != null && contentType.indexOf("vnd.wap.wml") != -1)
                    { // CMWap��ʱ������ʷ���ʾҳ��
                        InputStream input = null;
                        try
                        {
                            input = httpConn.getInputStream();
                            BufferedInputStream buffInput = new BufferedInputStream(input);
                            ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
                            byte[] b = new byte[2 * 1024];
                            int len;
                            while ((len = buffInput.read(b)) > 0)
                            {
                                tempOutput.write(b, 0, len);
                            }
                            byte[] wmlData = tempOutput.toByteArray();
                            String wmlStr = new String(wmlData, "UTF-8");
                            LogManager.logI(HttpConnectionManager.class, "parse the CMWap charge page...(base64 content:".concat(Base64.encode(wmlData)).concat(")"));
                            // �����ʷ���ʾҳ���е�URL
                            String parseUrl = null;
                            try
                            {
                                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                                XmlPullParser xmlParser = factory.newPullParser();
                                xmlParser.setInput(new StringReader(wmlStr));
                                boolean onEnterForward = false;
                                int eventType = xmlParser.getEventType();
                                while (eventType != XmlPullParser.END_DOCUMENT)
                                {
                                    switch (eventType)
                                    {
                                        case XmlPullParser.START_TAG:
                                            String tagName = xmlParser.getName().toLowerCase();
                                            if ("onevent".equals(tagName))
                                            {
                                                String s = xmlParser.getAttributeValue(null, "type").toLowerCase();
                                                if ("onenterforward".equals(s))
                                                    onEnterForward = true;
                                            } else if ("go".equals(tagName))
                                            {
                                                if (onEnterForward)
                                                    parseUrl = xmlParser.getAttributeValue(null, "href");
                                            }
                                            break;
                                    }
                                    if (parseUrl != null)
                                        break;
                                    eventType = xmlParser.next();
                                }
                            } catch (Exception e)
                            {
                                LogManager.logW(HttpConnectionManager.class, "parse CMWap charge page failed", e);
                            }
                            if (parseUrl == null || parseUrl.equals(""))
                            {
                                LogManager.logW(HttpConnectionManager.class, "could not parse url from CMWap charge page,would use the original url to try again...");
                                parseUrl = url;
                            }
                            return openConnection(parseUrl, urlEnc, method, followRedirects, connOrReadTimeout, currentRedirectCount, ++currentCMWapChargePageCount, requestHeaders, postData);
                        } finally
                        {
                            try
                            {
                                if (input != null)
                                    input.close();
                            } finally
                            {
                                httpConn.disconnect();
                            }
                        }
                    }
                }
                if (isKeepSession)
                    saveSession(originalUrl, httpConn.getHeaderFields()); // ��Ҫʹ��ԭʼURL����session
                return httpConn;
            } else if (rspCode == HttpURLConnection.HTTP_MOVED_PERM || rspCode == HttpURLConnection.HTTP_MOVED_TEMP || rspCode == HttpURLConnection.HTTP_SEE_OTHER)
            {
                if (!followRedirects)
                    return httpConn;
                // implements 'followRedirects' by myself,because the method of setFollowRedirects and setInstanceFollowRedirects have existed some problems.
                String location = httpConn.getHeaderField(HEADER_RESPONSE_LOCATION);
                if (location == null)
                    throw new IOException("redirects failed:could not find the location header");
                if (location.toLowerCase().indexOf(originalUrl.getProtocol() + "://") < 0)
                    location = originalUrl.getProtocol() + "://" + originalUrl.getHost() + location;
                httpConn.disconnect();
                LogManager.logI(HttpConnectionManager.class, "follow redirects...");
                return openConnection(location, urlEnc, "GET", followRedirects, connOrReadTimeout, ++currentRedirectCount, currentCMWapChargePageCount, requestHeaders, null);
            } else
            {
                throw new IOException("requesting returns http code:" + rspCode);
            }
        } catch (IOException e)
        {
            try
            {
                if (output != null)
                    output.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
            throw e;
        } catch (Exception e)
        {
            try
            {
                if (output != null)
                    output.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>����url�ӻ����в����ܹ�ά��session��cookieֵ�����ֵ֮���÷ֺŷָ�����δ�ҵ�������null
     * 
     * @param url
     * @return
     */
    public static String querySession(URL url)
    {
        String host = url.getHost().toLowerCase();
        if (host.equals("localhost"))
            host = "127.0.0.1";
        int port = url.getPort();
        if (port == -1)
            port = url.getDefaultPort();
        String authority = host.concat(":").concat(String.valueOf(port));
        String path = url.getPath();
        List<String> sessionCookies = null;
        if (path.equals("") || path.equals("/"))
        {
            sessionCookies = sessions.get(authority);
        } else
        {
            int index = path.indexOf("/", 1);
            if (index != -1)
                path = path.substring(0, index);
            sessionCookies = sessions.get(authority.concat(path));
            if (sessionCookies == null)
            {
                sessionCookies = sessions.get(authority);
            }
        }
        if (sessionCookies == null)
            return null;
        StringBuffer sessionCookiesBuffer = new StringBuffer();
        for (int i = 0; i < sessionCookies.size(); i++)
        {
            if (i != 0)
                sessionCookiesBuffer.append(";");
            sessionCookiesBuffer.append(sessionCookies.get(i));
        }
        return sessionCookiesBuffer.toString();
    }

    /**
     * <p>��������ά��session��cookieֵ�����ֵ֮���÷ֺŷָ�
     * 
     * @param url
     * @param sessionCookiesStr
     */
    public static void saveSession(URL url, String sessionCookiesStr)
    {
        List<String> sessionCookies = new ArrayList<String>();
        String[] sessionCookiesArr = sessionCookiesStr.split(";");
        for (String sessionCookie : sessionCookiesArr)
        {
            sessionCookies.add(sessionCookie);
        }
        saveSession(url, sessionCookies);
    }

    /**
     * <p>���浱ǰ��Ӧͷ������ά��session��cookieֵ
     * 
     * @param url
     * @param responseHeaders
     */
    private static void saveSession(URL url, Map<String, List<String>> responseHeaders)
    {
        if (responseHeaders != null)
        {
            List<String> cookies = responseHeaders.get(HEADER_RESPONSE_SET_COOKIE.toLowerCase()); // ��Androidƽ̨��ʵ���б�����Сд��key����ȡ��List��ʽ���ص���Ӧͷ
            if (cookies != null)
            {
                List<String> sessionCookies = new ArrayList<String>();
                for (String cookie : cookies)
                {
                    if (cookie != null)
                    {
                        String[] cookieArr = cookie.split(";");
                        String sessionCookie = null;
                        for (String perCookie : cookieArr)
                        {
                            perCookie = perCookie.trim();
                            if (perCookie.startsWith("JSESSIONID=") || perCookie.startsWith("PHPSESSID="))
                            {
                                sessionCookie = perCookie;
                                break;
                            }
                        }
                        if (sessionCookie == null)
                            sessionCookie = cookieArr[0].trim();
                        LogManager.logI(HttpConnectionManager.class, "prepare to save session(" + sessionCookie + ") for url " + url + "...");
                        sessionCookies.add(sessionCookie);
                    }
                }
                saveSession(url, sessionCookies);
            }
        }
    }

    /**
     * <p>��������ά��session��cookieֵ
     * 
     * @param url
     * @param sessionCookies
     */
    private static void saveSession(URL url, List<String> sessionCookies)
    {
        if (sessionCookies.size() == 0)
            return;
        String host = url.getHost().toLowerCase();
        if (host.equals("localhost"))
            host = "127.0.0.1";
        int port = url.getPort();
        if (port == -1)
            port = url.getDefaultPort();
        String authority = host.concat(":").concat(String.valueOf(port));
        String path = url.getPath();
        if (path.equals("") || path.equals("/"))
        {
            sessions.put(authority, sessionCookies);
        } else
        {
            int index = path.indexOf("/", 1);
            if (index != -1)
                path = path.substring(0, index);
            sessions.put(authority.concat(path), sessionCookies);
        }
    }

    private static class MyX509TrustManager implements X509TrustManager
    {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
        {
            // TODO Auto-generated method stub
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
        {
            // TODO Auto-generated method stub
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private static class MyHostnameVerifier implements HostnameVerifier
    {
        private String hostname;

        public MyHostnameVerifier(String hostname)
        {
            this.hostname = hostname;
        }

        @Override
        public boolean verify(String hostname, SSLSession session)
        {
            // TODO Auto-generated method stub
            if (this.hostname.equals(hostname))
                return true;
            return false;
        }
    }

}
