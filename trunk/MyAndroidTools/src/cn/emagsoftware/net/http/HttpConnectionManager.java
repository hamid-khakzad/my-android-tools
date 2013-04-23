package cn.emagsoftware.net.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import cn.emagsoftware.net.NetManager;
import cn.emagsoftware.net.URLManager;
import cn.emagsoftware.telephony.TelephonyMgr;
import cn.emagsoftware.util.FileUtilities;
import cn.emagsoftware.util.LogManager;

/**
 * Http Connection Manager
 * 
 * @author Wendell
 * @version 6.0
 */
public final class HttpConnectionManager
{
    public static final String HEADER_REQUEST_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HEADER_REQUEST_CONNECTION      = "Connection";
    public static final String HEADER_REQUEST_CACHE_CONTROL   = "Cache-Control";
    public static final String HEADER_REQUEST_ACCEPT_CHARSET  = "Accept-Charset";
    public static final String HEADER_REQUEST_CONTENT_TYPE    = "Content-Type";
    public static final String HEADER_REQUEST_CONTENT_LENGTH  = "Content-Length";
    public static final String HEADER_REQUEST_USER_AGENT      = "User-Agent";
    public static final String HEADER_REQUEST_COOKIE          = "Cookie";

    public static final String HEADER_RESPONSE_CONTENT_TYPE   = "Content-Type";
    public static final String HEADER_RESPONSE_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_RESPONSE_LOCATION       = "Location";
    public static final String HEADER_RESPONSE_SET_COOKIE     = "Set-Cookie";

    public static final int    REDIRECT_MAX_COUNT             = 10;
    public static final int    CMWAP_CHARGEPAGE_MAX_COUNT     = 3;

    private static Context     appContext                     = null;
    private static boolean     acceptCookie                   = true;
    private static boolean     useConcatURLModeWhenCMWap      = false;
    private static boolean     ignoreChargePageWhenCMWap      = false;

    private HttpConnectionManager()
    {
    }

    public static void bindApplicationContext(Context context)
    {
        context = context.getApplicationContext();
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        // 删除过期的Cookie，尽管该方法在Android4.0以下是异步实现且bindApplicationContext方法执行不定期，但过期的Cookie是无法取出的，该方法的目的还是达到了。该方法无需同步
        cookieManager.removeExpiredCookie();
        HttpConnectionManager.appContext = context;
    }

    public static void setAcceptCookie(boolean accept)
    {
        // CookieManager的setAcceptCookie从Android4.0开始，不再能够在外部使用，所以这里统一使用外部参数控制
        HttpConnectionManager.acceptCookie = accept;
    }

    /**
     * <p>设置在使用中国移动CMWap时是否使用拼接URL的模式
     * 
     * @param useConcatURLModeWhenCMWap
     */
    public static void setUseConcatURLModeWhenCMWap(boolean useConcatURLModeWhenCMWap)
    {
        HttpConnectionManager.useConcatURLModeWhenCMWap = useConcatURLModeWhenCMWap;
    }

    /**
     * <p>设置在使用中国移动CMWap时是否忽略CMWap的资费页面
     * 
     * @param ignore
     */
    public static void ignoreChargePageWhenCMWap(boolean ignore)
    {
        HttpConnectionManager.ignoreChargePageWhenCMWap = ignore;
    }

    /**
     * 进行Http Get请求
     * 
     * @param url 请求的url
     * @param followRedirects 是否自动重定向
     * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
     * @param requestHeaders 请求头，不需要时可传null
     * @return HttpResponseResultStream实例
     * @throws IOException
     */
    public static HttpResponseResultStream doGetForStream(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders) throws IOException
    {
        HttpURLConnection httpConn = null;
        InputStream input = null;
        try
        {
            httpConn = openConnection(url, "GET", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, null);
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

    public static HttpResponseResult doGet(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders) throws IOException
    {
        HttpResponseResultStream result = doGetForStream(url, followRedirects, connOrReadTimeout, requestHeaders);
        result.generateData();
        return result;
    }

    /**
     * 进行Http Post请求，将以值为application/x-www-form-urlencoded的Content-Type来提交键值对参数
     * 
     * @param url 请求的url
     * @param followRedirects 是否自动重定向
     * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
     * @param requestHeaders 请求头，不需要时可传null
     * @param postParams 提交的POST参数，不需要时可传null
     * @param postParamsEnc 对提交的POST参数进行URL编码的字符集，不需要URL编码时可传null
     * @return HttpResponseResultStream实例
     * @throws IOException
     */
    public static HttpResponseResultStream doPostForStream(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, Map<String, String> postParams,
            String postParamsEnc) throws IOException
    {
        HttpURLConnection httpConn = null;
        InputStream input = null;
        try
        {
            if (requestHeaders == null)
                requestHeaders = new HashMap<String, List<String>>();
            List<String> contentTypes = new ArrayList<String>(); // Http规范规定Content-Type只能有一个
            contentTypes.add("application/x-www-form-urlencoded");
            requestHeaders.put(HEADER_REQUEST_CONTENT_TYPE, contentTypes);
            InputStream paramsData = null;
            if (postParams != null)
            {
                String postParamsStr = URLManager.concatParams(postParams, postParamsEnc);
                paramsData = new ByteArrayInputStream(postParamsStr.getBytes()); // 经过外部或内部URL编码之后的参数只含英文，可用任意字符集进行解码
            }
            httpConn = openConnection(url, "POST", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, paramsData);
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

    public static HttpResponseResult doPost(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, Map<String, String> postParams, String postParamsEnc)
            throws IOException
    {
        HttpResponseResultStream result = doPostForStream(url, followRedirects, connOrReadTimeout, requestHeaders, postParams, postParamsEnc);
        result.generateData();
        return result;
    }

    /**
     * 进行Http Post请求，将以值为application/octet-stream的Content-Type来提交数据
     * 
     * @param url 请求的url
     * @param followRedirects 是否自动重定向
     * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
     * @param requestHeaders 请求头，不需要时可传null
     * @param postData 提交的POST数据，不需要时可传null
     * @return HttpResponseResultStream实例
     * @throws IOException
     */
    public static HttpResponseResultStream doPostForStream(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, InputStream postData)
            throws IOException
    {
        HttpURLConnection httpConn = null;
        InputStream input = null;
        try
        {
            if (requestHeaders == null)
                requestHeaders = new HashMap<String, List<String>>();
            List<String> contentTypes = new ArrayList<String>(); // Http规范规定Content-Type只能有一个
            contentTypes.add("application/octet-stream");
            requestHeaders.put(HEADER_REQUEST_CONTENT_TYPE, contentTypes);
            if (postData == null)
                postData = new ByteArrayInputStream(new byte[] {}); // 貌似针对application/octet-stream的情况必须这样处理，否则会抛出FileNotFoundException，这可能是Android的底层实现有缺陷
            httpConn = openConnection(url, "POST", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, postData);
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

    public static HttpResponseResult doPost(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, InputStream postData) throws IOException
    {
        HttpResponseResultStream result = doPostForStream(url, followRedirects, connOrReadTimeout, requestHeaders, postData);
        result.generateData();
        return result;
    }

    /**
     * 返回HttpURLConnection实例
     * 
     * @param url 请求的url
     * @param method 请求的方式，如GET,POST
     * @param followRedirects 是否自动重定向
     * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
     * @param currentRedirectCount 当前是第几次重定向
     * @param currentCMWapChargePageCount 当前是第几次出现CMWap资费提示页面
     * @param requestHeaders 请求头，不需要时可传null
     * @param postData method为POST时提交的数据，不需要时可传null
     * @return HttpURLConnection实例
     * @throws IOException
     */
    private static HttpURLConnection openConnection(String url, String method, boolean followRedirects, int connOrReadTimeout, int currentRedirectCount, int currentCMWapChargePageCount,
            Map<String, List<String>> requestHeaders, InputStream postData) throws IOException
    {
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        if (currentRedirectCount < 0)
            throw new IllegalArgumentException("current redirect count can not set to below zero");
        if (currentRedirectCount > REDIRECT_MAX_COUNT)
            throw new IOException("too many redirect times");
        if (currentCMWapChargePageCount < 0)
            throw new IllegalArgumentException("current cmwap charge page count can not set to below zero");
        if (currentCMWapChargePageCount > CMWAP_CHARGEPAGE_MAX_COUNT)
            throw new IOException("too many showing cmwap charge page times");
        URL originalURL = new URL(url);
        URL myURL = originalURL;
        String concatHost = null;
        Proxy proxy = null;
        NetworkInfo curNetwork = NetManager.getActiveNetworkInfo(appContext);
        if (curNetwork != null)
        {
            if (curNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
            {
                if (useConcatURLModeWhenCMWap && "CMWAP".equals(NetManager.getNetworkDetailType(curNetwork)))
                {
                    concatHost = myURL.getAuthority();
                    String myURLStr = "http://10.0.0.172".concat(myURL.getPath());
                    String query = myURL.getQuery();
                    if (query != null)
                        myURLStr = myURLStr.concat("?").concat(query);
                    myURL = new URL(myURLStr);
                } else
                {
                    String host = android.net.Proxy.getDefaultHost();
                    int port = android.net.Proxy.getDefaultPort();
                    if (host != null && port != -1)
                    {
                        if (TelephonyMgr.isOPhone20()) // OPhone2.0的特殊情况
                        {
                            String detailType = NetManager.getNetworkDetailType(curNetwork);
                            if ("CMWAP".equals(detailType) || "UNIWAP".equals(detailType) || "CTWAP".equals(detailType))
                            {
                                InetSocketAddress inetAddress = new InetSocketAddress(host, port);
                                Type proxyType = Type.valueOf(myURL.getProtocol().toUpperCase());
                                proxy = new Proxy(proxyType, inetAddress);
                            }
                        } else
                        {
                            InetSocketAddress inetAddress = new InetSocketAddress(host, port);
                            Type proxyType = Type.valueOf(myURL.getProtocol().toUpperCase());
                            proxy = new Proxy(proxyType, inetAddress);
                        }
                    }
                }
            }
        }
        HttpURLConnection httpConn = null;
        OutputStream output = null;
        try
        {
            LogManager.logI(HttpConnectionManager.class, "request url ".concat(myURL.toString()).concat("..."));
            if ("https".equals(myURL.getProtocol()))
            {
                SSLContext sslCont = SSLContext.getInstance("TLS");
                sslCont.init(null, new TrustManager[] { new MyX509TrustManager() }, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslCont.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier(myURL.getHost()));
                if (proxy == null)
                    httpConn = (HttpsURLConnection) myURL.openConnection();
                else
                    httpConn = (HttpsURLConnection) myURL.openConnection(proxy);
            } else
            {
                if (proxy == null)
                    httpConn = (HttpURLConnection) myURL.openConnection();
                else
                    httpConn = (HttpURLConnection) myURL.openConnection(proxy);
            }
            httpConn.setRequestMethod(method);
            HttpURLConnection.setFollowRedirects(false);
            httpConn.setInstanceFollowRedirects(false);
            httpConn.setDoInput(true);
            if (method.equalsIgnoreCase("POST"))
                httpConn.setDoOutput(true); // 经测试，在Android4.0且某些特殊的服务器实现下，如果总是setDoOutput(true)可能收到405的Http状态码
            else
                httpConn.setDoOutput(false); // 经测试，在Android4.0且某些特殊的服务器实现下，如果不设置setDoOutput(false)可能收到405的Http状态码
            httpConn.setReadTimeout(connOrReadTimeout);
            httpConn.setConnectTimeout(connOrReadTimeout);
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
            String cookies = getCookies(url); // 需要使用原始URL获取Cookies
            if (cookies != null)
            {
                LogManager.logI(HttpConnectionManager.class, "set cookies(" + cookies + ") to url " + url);
                httpConn.setRequestProperty(HEADER_REQUEST_COOKIE, cookies);
            }
            if (method.equalsIgnoreCase("POST") && postData != null)
            {
                output = httpConn.getOutputStream();
                FileUtilities.readAndWrite(postData, output, 1024 * 2);
                output.close();
            }
            if (acceptCookie)
            {
                Map<String, List<String>> headerFields = httpConn.getHeaderFields();
                if (headerFields != null)
                    addCookies(url, headerFields); // 需要使用原始URL添加Cookies
            }
            int rspCode = httpConn.getResponseCode();
            if (rspCode == HttpURLConnection.HTTP_MOVED_PERM || rspCode == HttpURLConnection.HTTP_MOVED_TEMP || rspCode == HttpURLConnection.HTTP_SEE_OTHER)
            {
                if (!followRedirects)
                    return httpConn;
                // 自己实现Follow Redirects，自带的setFollowRedirects和setInstanceFollowRedirects方法存在一些问题
                String location = httpConn.getHeaderField(HEADER_RESPONSE_LOCATION);
                if (location == null)
                    throw new IOException("redirects failed:could not find the location header");
                if (location.toLowerCase().indexOf(originalURL.getProtocol() + "://") < 0)
                    location = originalURL.getProtocol() + "://" + originalURL.getHost() + location;
                httpConn.disconnect();
                LogManager.logI(HttpConnectionManager.class, "follow redirects...");
                return openConnection(location, "GET", followRedirects, connOrReadTimeout, ++currentRedirectCount, currentCMWapChargePageCount, requestHeaders, null);
            } else if (rspCode >= 400)
            {
                throw new IOException("requesting returns error http code:" + rspCode);
            } else
            {
                if ((concatHost != null || proxy != null) && !ignoreChargePageWhenCMWap)
                {
                    String contentType = httpConn.getHeaderField(HEADER_RESPONSE_CONTENT_TYPE);
                    if (contentType != null && contentType.indexOf("vnd.wap.wml") != -1)
                    { // CMWap有时会出现资费提示页面
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
                            LogManager.logI(HttpConnectionManager.class, "parse the cmwap charge page...(utf-8 content:".concat(wmlStr).concat(")"));
                            // 解析资费提示页面中的URL
                            String parseURL = null;
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
                                                    parseURL = xmlParser.getAttributeValue(null, "href");
                                            }
                                            break;
                                    }
                                    if (parseURL != null)
                                        break;
                                    eventType = xmlParser.next();
                                }
                            } catch (Exception e)
                            {
                                LogManager.logW(HttpConnectionManager.class, "parse cmwap charge page failed", e);
                            }
                            if (parseURL == null || parseURL.equals(""))
                            {
                                LogManager.logW(HttpConnectionManager.class, "could not parse url from cmwap charge page,would use the original url to try again...");
                                parseURL = url;
                            }
                            return openConnection(parseURL, method, followRedirects, connOrReadTimeout, currentRedirectCount, ++currentCMWapChargePageCount, requestHeaders, postData);
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

    /**
     * <p>删除所有的Cookies
     */
    public static void removeAllCookies()
    {
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        CookieManager.getInstance().removeAllCookie(); // 无需同步
        if (!TelephonyMgr.isAndroid4Above()) // Android4.0以下是异步实现，所以无奈只能等待
        {
            try
            {
                Thread.sleep(200);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <p>删除所有的Session Cookies，即没有expires的Cookies
     */
    public static void removeSessionCookies()
    {
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        CookieManager.getInstance().removeSessionCookie(); // 无需同步
        if (!TelephonyMgr.isAndroid4Above()) // Android4.0以下是异步实现，所以无奈只能等待
        {
            try
            {
                Thread.sleep(200);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <p>根据URL获取Cookies <p>不同Cookie之间使用"分号+空格"的方式分隔
     * 
     * @param url
     * @return
     */
    public static String getCookies(String url)
    {
        if (!acceptCookie)
            return null;
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        return CookieManager.getInstance().getCookie(url);
    }

    /**
     * <p>添加指定的Cookie <p>Cookie的作用范围由其domain和path决定，无domain时默认为当前URL的域，path是一个相对于domain的路径，无path时默认为当前URL的路径(以"/"结尾时即为自己，否则为其直接上级) <p>domain和path形成一个Base Domain，Base Domain具有向下继承性，在该Base
     * Domain下任何子级的URL都继承该Cookie <p><b>使用该方法时要格外小心</b>，因为只有相同Base Domain下的同名Cookie才会被替换，这可能出现添加的Cookie不能替换之前的同名Cookie，或者通过网络请求自动添加的Cookie不能替换该方法添加的同名Cookie的情况，从而导致一个URL对应多个同名Cookie的现象
     * 
     * @param url
     * @param cookie
     */
    public static void addCookie(String url, String cookie)
    {
        if (!acceptCookie)
            return;
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        CookieManager.getInstance().setCookie(url, cookie);
        if (!TelephonyMgr.isAndroid4Above()) // Android4.0以上会通过JNI自动快速同步，为使行为一致，4.0以下版本将手工进行异步快速同步(Cookie同步是单向的，只会从Ram到Flash，从而保证了当前Cookie不受外部影响)
            CookieSyncManager.getInstance().sync();
    }

    /**
     * <p>添加当前响应头中的Cookies
     * 
     * @param url
     * @param responseHeaders
     */
    private static void addCookies(String url, Map<String, List<String>> responseHeaders)
    {
        List<String> cookies = responseHeaders.get(HEADER_RESPONSE_SET_COOKIE.toLowerCase()); // 在Android平台的实现中必须以小写的key来获取以List形式返回的响应头
        if (cookies != null)
        {
            CookieManager cookieManager = CookieManager.getInstance();
            boolean shouldSync = false;
            for (String cookie : cookies)
            {
                if (cookie != null)
                {
                    shouldSync = true;
                    LogManager.logI(HttpConnectionManager.class, "got cookie(" + cookie + ") from url " + url);
                    cookieManager.setCookie(url, cookie);
                }
            }
            if (shouldSync && !TelephonyMgr.isAndroid4Above()) // Android4.0以上会通过JNI自动快速同步，为使行为一致，4.0以下版本将手工进行异步快速同步(Cookie同步是单向的，只会从Ram到Flash，从而保证了当前Cookie不受外部影响)
                CookieSyncManager.getInstance().sync();
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
