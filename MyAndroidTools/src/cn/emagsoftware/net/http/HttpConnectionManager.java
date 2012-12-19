package cn.emagsoftware.net.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import cn.emagsoftware.net.NetManager;
import cn.emagsoftware.telephony.TelephonyMgr;
import cn.emagsoftware.util.LogManager;

/**
 * Http Connection Manager
 * 
 * @author Wendell
 * @version 5.1
 */
public final class HttpConnectionManager
{
    public static final String         HEADER_REQUEST_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String         HEADER_REQUEST_CONNECTION      = "Connection";
    public static final String         HEADER_REQUEST_CACHE_CONTROL   = "Cache-Control";
    public static final String         HEADER_REQUEST_ACCEPT_CHARSET  = "Accept-Charset";
    public static final String         HEADER_REQUEST_CONTENT_TYPE    = "Content-Type";
    public static final String         HEADER_REQUEST_USER_AGENT      = "User-Agent";
    public static final String         HEADER_REQUEST_COOKIE          = "Cookie";

    public static final String         HEADER_RESPONSE_CONTENT_TYPE   = "Content-Type";
    public static final String         HEADER_RESPONSE_LOCATION       = "Location";
    public static final String         HEADER_RESPONSE_SET_COOKIE     = "Set-Cookie";

    public static final int            REDIRECT_MAX_COUNT             = 10;
    public static final int            CMWAP_CHARGEPAGE_MAX_COUNT     = 3;

    private static Context             appContext                     = null;
    private static boolean             keepSession                    = true;
    private static Map<String, String> sessions                       = new Hashtable<String, String>();
    private static boolean             useConcatUrlModeWhenCMWap      = false;
    private static boolean             ignoreChargePageWhenCMWap      = false;

    private HttpConnectionManager()
    {
    }

    public static void bindAppCtxForCheckNetworkType(Context context)
    {
        HttpConnectionManager.appContext = context.getApplicationContext();
    }

    public static void setKeepSession(boolean keepSession)
    {
        HttpConnectionManager.keepSession = keepSession;
    }

    /**
     * <p>������ʹ���й��ƶ�CMWapʱ�Ƿ�ʹ��ƴ��Url��ģʽ
     * 
     * @param useConcatUrlModeWhenCMWap
     */
    public static void setUseConcatUrlModeWhenCMWap(boolean useConcatUrlModeWhenCMWap)
    {
        HttpConnectionManager.useConcatUrlModeWhenCMWap = useConcatUrlModeWhenCMWap;
    }

    /**
     * <p>������ʹ���й��ƶ�CMWapʱ�Ƿ����CMWap���ʷ�ҳ��
     * 
     * @param ignore
     */
    public static void ignoreChargePageWhenCMWap(boolean ignore)
    {
        HttpConnectionManager.ignoreChargePageWhenCMWap = ignore;
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
            List<String> contentTypes = new ArrayList<String>(); // http�淶�涨Content-Typeֻ����һ��
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
            List<String> contentTypes = new ArrayList<String>(); // http�淶�涨Content-Typeֻ����һ��
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
        String concatHost = null;
        Proxy proxy = null;
        if (appContext != null)
        {
            NetworkInfo curNetwork = NetManager.getActiveNetworkInfo(appContext);
            if (curNetwork != null)
            {
                if (curNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                {
                    if (useConcatUrlModeWhenCMWap && "CMWAP".equals(NetManager.getNetworkDetailType(curNetwork)))
                    {
                        concatHost = myUrl.getAuthority();
                        String myUrlStr = "http://10.0.0.172".concat(myUrl.getPath());
                        String query = myUrl.getQuery();
                        if (query != null)
                            myUrlStr = myUrlStr.concat("?").concat(query);
                        myUrl = new URL(myUrlStr);
                    } else
                    {
                        String host = android.net.Proxy.getDefaultHost();
                        int port = android.net.Proxy.getDefaultPort();
                        if (host != null && port != -1)
                        {
                            if (TelephonyMgr.isOPhone20()) // OPhone 2.0���������
                            {
                                String detailType = NetManager.getNetworkDetailType(curNetwork);
                                if ("CMWAP".equals(detailType) || "UNIWAP".equals(detailType) || "CTWAP".equals(detailType))
                                {
                                    InetSocketAddress inetAddress = new InetSocketAddress(host, port);
                                    Type proxyType = Type.valueOf(myUrl.getProtocol().toUpperCase());
                                    proxy = new Proxy(proxyType, inetAddress);
                                }
                            } else
                            {
                                InetSocketAddress inetAddress = new InetSocketAddress(host, port);
                                Type proxyType = Type.valueOf(myUrl.getProtocol().toUpperCase());
                                proxy = new Proxy(proxyType, inetAddress);
                            }
                        }
                    }
                }
            }
        }
        HttpURLConnection httpConn = null;
        OutputStream output = null;
        try
        {
            LogManager.logI(HttpConnectionManager.class, "request url '".concat(myUrl.toString()).concat("'..."));
            if ("https".equals(myUrl.getProtocol()))
            {
                SSLContext sslCont = SSLContext.getInstance("TLS");
                sslCont.init(null, new TrustManager[] { new MyX509TrustManager() }, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslCont.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier(myUrl.getHost()));
                if (proxy == null)
                    httpConn = (HttpsURLConnection) myUrl.openConnection();
                else
                    httpConn = (HttpsURLConnection) myUrl.openConnection(proxy);
            } else
            {
                if (proxy == null)
                    httpConn = (HttpURLConnection) myUrl.openConnection();
                else
                    httpConn = (HttpURLConnection) myUrl.openConnection(proxy);
            }
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
            if (keepSession)
            {
                String session = querySession(originalUrl); // ��Ҫʹ��ԭʼURL��ѯsession
                if (session != null)
                {
                    LogManager.logI(HttpConnectionManager.class, "queried session(" + session + ") for url " + originalUrl);
                    httpConn.addRequestProperty(HEADER_REQUEST_COOKIE, session);
                }
            }
            if (concatHost != null)
            {
                httpConn.addRequestProperty("X-Online-Host", concatHost);
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
            if (rspCode >= 400)
            {
                throw new IOException("requesting returns error http code:" + rspCode);
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
                if ((concatHost != null || proxy != null) && !ignoreChargePageWhenCMWap)
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
                            String wmlStr = new String(tempOutput.toByteArray(), "UTF-8");
                            LogManager.logI(HttpConnectionManager.class, "parse the CMWap charge page...(utf-8 content:".concat(wmlStr).concat(")"));
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
                if (keepSession)
                {
                    Map<String, List<String>> headerFields = httpConn.getHeaderFields();
                    if (headerFields != null)
                        saveSession(originalUrl, headerFields); // ��Ҫʹ��ԭʼURL����session
                }
                return httpConn;
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

    public static void clearSessions()
    {
        sessions.clear();
    }

    public static boolean clearSession(URL url)
    {
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
            return sessions.remove(authority) != null;
        } else
        {
            int index = path.indexOf("/", 1);
            if (index != -1)
                path = path.substring(0, index);
            return sessions.remove(authority.concat(path)) != null;
        }
    }

    /**
     * <p>����url�ӻ����в����ܹ�ά��session��cookieֵ����δ�ҵ�������null
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
        String sessionCookie = null;
        if (path.equals("") || path.equals("/"))
        {
            sessionCookie = sessions.get(authority);
        } else
        {
            int index = path.indexOf("/", 1);
            if (index != -1)
                path = path.substring(0, index);
            sessionCookie = sessions.get(authority.concat(path));
            if (sessionCookie == null) // ���Ҳ���ʱ�����ظ�url��session cookie
            {
                sessionCookie = sessions.get(authority);
            }
        }
        return sessionCookie;
    }

    /**
     * <p>���浱ǰ��Ӧͷ������ά��session��cookieֵ
     * 
     * @param url
     * @param responseHeaders
     */
    private static void saveSession(URL url, Map<String, List<String>> responseHeaders)
    {
        List<String> cookies = responseHeaders.get(HEADER_RESPONSE_SET_COOKIE.toLowerCase()); // ��Androidƽ̨��ʵ���б�����Сд��key����ȡ��List��ʽ���ص���Ӧͷ
        if (cookies != null)
        {
            String sessionCookie = null;
            ListIterator<String> sessionCookies = cookies.listIterator(cookies.size());
            a: while (sessionCookies.hasPrevious()) // ���򣬰�����Ҫȡ���һ��session cookie
            {
                String cookie = sessionCookies.previous();
                if (cookie != null)
                {
                    String[] cookieArr = cookie.split(";");
                    for (int i = cookieArr.length - 1; i >= 0; i--) // ���򣬰�����Ҫȡ���һ��session cookie
                    {
                        String perCookie = cookieArr[i].trim();
                        if (perCookie.startsWith("JSESSIONID=") || perCookie.startsWith("PHPSESSID="))
                        {
                            sessionCookie = perCookie;
                            break a;
                        }
                    }
                }
            }
            if (sessionCookie != null) // δ�ҵ�session cookieʱ�������ԭ����ֵ������������ֻ����session�����ı�ʱ�Ż���ͻ��˷����µ�session cookie
            {
                LogManager.logI(HttpConnectionManager.class, "prepare to save session(" + sessionCookie + ") for url " + url.toString() + "...");
                saveSession(url, sessionCookie);
            }
        }
    }

    /**
     * <p>��������ά��session��cookieֵ
     * 
     * @param url
     * @param sessionCookie
     */
    public static void saveSession(URL url, String sessionCookie)
    {
        if (sessionCookie == null)
            throw new NullPointerException();
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
            sessions.put(authority, sessionCookie);
        } else
        {
            int index = path.indexOf("/", 1);
            if (index != -1)
                path = path.substring(0, index);
            sessions.put(authority.concat(path), sessionCookie);
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
