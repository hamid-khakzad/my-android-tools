package cn.emagsoftware.net.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class HttpResponseResultStream extends HttpResponseResult
{

    protected InputStream       resultStream = null;
    protected HttpURLConnection httpURLConn  = null;

    public InputStream getResultStream()
    {
        return resultStream;
    }

    public void setResultStream(InputStream resultStream)
    {
        this.resultStream = resultStream;
    }

    public HttpURLConnection getHttpURLConn()
    {
        return httpURLConn;
    }

    public void setHttpURLConn(HttpURLConnection httpURLConn)
    {
        this.httpURLConn = httpURLConn;
    }

    /**
     * <p>将会把数据全部读到内存，对于数据比较大的情况要慎用
     */
    public void generateData() throws IOException
    {
        try
        {
            BufferedInputStream buffInput = new BufferedInputStream(resultStream);
            ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
            byte[] b = new byte[2 * 1024];
            int len;
            while ((len = buffInput.read(b)) > 0)
            {
                tempOutput.write(b, 0, len);
            }
            setData(tempOutput.toByteArray());
        } finally
        {
            close();
        }
    }

    public void close() throws IOException
    {
        try
        {
            if (resultStream != null)
                resultStream.close();
        } finally
        {
            if (httpURLConn != null)
                httpURLConn.disconnect();
        }
    }

}
