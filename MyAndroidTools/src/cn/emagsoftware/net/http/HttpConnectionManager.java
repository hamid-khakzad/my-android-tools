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
        // ɾ�����ڵ�Cookie�����ܸ÷�����Android4.0�������첽ʵ����bindApplicationContext����ִ�в����ڣ������ڵ�Cookie���޷�ȡ���ģ��÷�����Ŀ�Ļ��Ǵﵽ�ˡ��÷�������ͬ��
        cookieManager.removeExpiredCookie();
        HttpConnectionManager.appContext = context;
    }

    public static void setAcceptCookie(boolean accept)
    {
        // CookieManager��setAcceptCookie��Android4.0��ʼ�������ܹ����ⲿʹ�ã���������ͳһʹ���ⲿ��������
        HttpConnectionManager.acceptCookie = accept;
    }

    /**
     * <p>������ʹ���й��ƶ�CMWapʱ�Ƿ�ʹ��ƴ��URL��ģʽ
     * 
     * @param useConcatURLModeWhenCMWap
     */
    public static void setUseConcatURLModeWhenCMWap(boolean useConcatURLModeWhenCMWap)
    {
        HttpConnectionManager.useConcatURLModeWhenCMWap = useConcatURLModeWhenCMWap;
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
     * ����Http Get����
     * 
     * @param url �����url
     * @param followRedirects �Ƿ��Զ��ض���
     * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
     * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
     * @return HttpResponseResultStreamʵ��
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
     * ����Http Post���󣬽���ֵΪapplication/x-www-form-urlencoded��Content-Type���ύ��ֵ�Բ���
     * 
     * @param url �����url
     * @param followRedirects �Ƿ��Զ��ض���
     * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
     * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
     * @param postParams �ύ��POST����������Ҫʱ�ɴ�null
     * @param postParamsEnc ���ύ��POST��������URL������ַ���������ҪURL����ʱ�ɴ�null
     * @return HttpResponseResultStreamʵ��
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
            List<String> contentTypes = new ArrayList<String>(); // Http�淶�涨Content-Typeֻ����һ��
            contentTypes.add("application/x-www-form-urlencoded");
            requestHeaders.put(HEADER_REQUEST_CONTENT_TYPE, contentTypes);
            InputStream paramsData = null;
            if (postParams != null)
            {
                String postParamsStr = URLManager.concatParams(postParams, postParamsEnc);
                paramsData = new ByteArrayInputStream(postParamsStr.getBytes()); // �����ⲿ���ڲ�URL����֮��Ĳ���ֻ��Ӣ�ģ����������ַ������н���
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
     * ����Http Post���󣬽���ֵΪapplication/octet-stream��Content-Type���ύ����
     * 
     * @param url �����url
     * @param followRedirects �Ƿ��Զ��ض���
     * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
     * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
     * @param postData �ύ��POST���ݣ�����Ҫʱ�ɴ�null
     * @return HttpResponseResultStreamʵ��
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
            List<String> contentTypes = new ArrayList<String>(); // Http�淶�涨Content-Typeֻ����һ��
            contentTypes.add("application/octet-stream");
            requestHeaders.put(HEADER_REQUEST_CONTENT_TYPE, contentTypes);
            if (postData == null)
                postData = new ByteArrayInputStream(new byte[] {}); // ò�����application/octet-stream�����������������������׳�FileNotFoundException���������Android�ĵײ�ʵ����ȱ��
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
     * ����HttpURLConnectionʵ��
     * 
     * @param url �����url
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
                        if (TelephonyMgr.isOPhone20()) // OPhone2.0���������
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
                httpConn.setDoOutput(true); // �����ԣ���Android4.0��ĳЩ����ķ�����ʵ���£��������setDoOutput(true)�����յ�405��Http״̬��
            else
                httpConn.setDoOutput(false); // �����ԣ���Android4.0��ĳЩ����ķ�����ʵ���£����������setDoOutput(false)�����յ�405��Http״̬��
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
            String cookies = getCookies(url); // ��Ҫʹ��ԭʼURL��ȡCookies
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
                    addCookies(url, headerFields); // ��Ҫʹ��ԭʼURL���Cookies
            }
            int rspCode = httpConn.getResponseCode();
            if (rspCode == HttpURLConnection.HTTP_MOVED_PERM || rspCode == HttpURLConnection.HTTP_MOVED_TEMP || rspCode == HttpURLConnection.HTTP_SEE_OTHER)
            {
                if (!followRedirects)
                    return httpConn;
                // �Լ�ʵ��Follow Redirects���Դ���setFollowRedirects��setInstanceFollowRedirects��������һЩ����
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
                            LogManager.logI(HttpConnectionManager.class, "parse the cmwap charge page...(utf-8 content:".concat(wmlStr).concat(")"));
                            // �����ʷ���ʾҳ���е�URL
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
     * <p>ɾ�����е�Cookies
     */
    public static void removeAllCookies()
    {
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        CookieManager.getInstance().removeAllCookie(); // ����ͬ��
        if (!TelephonyMgr.isAndroid4Above()) // Android4.0�������첽ʵ�֣���������ֻ�ܵȴ�
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
     * <p>ɾ�����е�Session Cookies����û��expires��Cookies
     */
    public static void removeSessionCookies()
    {
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        CookieManager.getInstance().removeSessionCookie(); // ����ͬ��
        if (!TelephonyMgr.isAndroid4Above()) // Android4.0�������첽ʵ�֣���������ֻ�ܵȴ�
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
     * <p>����URL��ȡCookies <p>��ͬCookie֮��ʹ��"�ֺ�+�ո�"�ķ�ʽ�ָ�
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
     * <p>���ָ����Cookie <p>Cookie�����÷�Χ����domain��path��������domainʱĬ��Ϊ��ǰURL����path��һ�������domain��·������pathʱĬ��Ϊ��ǰURL��·��(��"/"��βʱ��Ϊ�Լ�������Ϊ��ֱ���ϼ�) <p>domain��path�γ�һ��Base Domain��Base Domain�������¼̳��ԣ��ڸ�Base
     * Domain���κ��Ӽ���URL���̳и�Cookie <p><b>ʹ�ø÷���ʱҪ����С��</b>����Ϊֻ����ͬBase Domain�µ�ͬ��Cookie�Żᱻ�滻������ܳ�����ӵ�Cookie�����滻֮ǰ��ͬ��Cookie������ͨ�����������Զ���ӵ�Cookie�����滻�÷�����ӵ�ͬ��Cookie��������Ӷ�����һ��URL��Ӧ���ͬ��Cookie������
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
        if (!TelephonyMgr.isAndroid4Above()) // Android4.0���ϻ�ͨ��JNI�Զ�����ͬ����Ϊʹ��Ϊһ�£�4.0���°汾���ֹ������첽����ͬ��(Cookieͬ���ǵ���ģ�ֻ���Ram��Flash���Ӷ���֤�˵�ǰCookie�����ⲿӰ��)
            CookieSyncManager.getInstance().sync();
    }

    /**
     * <p>��ӵ�ǰ��Ӧͷ�е�Cookies
     * 
     * @param url
     * @param responseHeaders
     */
    private static void addCookies(String url, Map<String, List<String>> responseHeaders)
    {
        List<String> cookies = responseHeaders.get(HEADER_RESPONSE_SET_COOKIE.toLowerCase()); // ��Androidƽ̨��ʵ���б�����Сд��key����ȡ��List��ʽ���ص���Ӧͷ
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
            if (shouldSync && !TelephonyMgr.isAndroid4Above()) // Android4.0���ϻ�ͨ��JNI�Զ�����ͬ����Ϊʹ��Ϊһ�£�4.0���°汾���ֹ������첽����ͬ��(Cookieͬ���ǵ���ģ�ֻ���Ram��Flash���Ӷ���֤�˵�ǰCookie�����ⲿӰ��)
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
