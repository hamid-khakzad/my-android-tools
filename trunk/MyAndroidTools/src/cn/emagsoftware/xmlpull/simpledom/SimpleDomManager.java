package cn.emagsoftware.xmlpull.simpledom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public final class SimpleDomManager
{

    private SimpleDomManager()
    {
    }

    public static String serializeDom(List<Element> dom, boolean containsXmlHead)
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String encoding = "utf-8";
        try
        {
            serializeDom(dom, containsXmlHead, output, encoding);
            return new String(output.toByteArray(), encoding);
        } catch (IOException e)
        {
            // 序列化到字符串一般不会出现IOException
            throw new RuntimeException(e);
        }
    }

    public static void serializeDom(List<Element> dom, boolean containsXmlHead, OutputStream output, String encoding) throws IOException
    {
        if (dom == null || output == null || encoding == null)
            throw new NullPointerException();
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(output, encoding);
        if (containsXmlHead)
            serializer.startDocument(encoding, true);
        serializeDomImpl(serializer, dom);
        serializer.endDocument();
    }

    private static void serializeDomImpl(XmlSerializer serializer, List<Element> dom) throws IOException
    {
        for (Element element : dom)
        {
            String tag = element.getTag();
            serializer.startTag(null, tag);
            List<String[]> attrs = element.getAttributes();
            for (String[] attr : attrs)
            {
                serializer.attribute(null, attr[0], attr[1]);
            }
            if (element.isLeaf())
            {
                serializer.text(element.getText());
            } else
            {
                serializeDomImpl(serializer, element.getChildren());
            }
            serializer.endTag(null, tag);
        }
    }

    public static List<Element> parseData(String data) throws XmlPullParserException
    {
        if (data == null)
            throw new NullPointerException();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(data));
        List<Element> dom = new ArrayList<Element>();
        try
        {
            parseDataImpl(parser, dom);
        } catch (IOException e)
        {
            // 从字符串解析一般不会出现IOException
            throw new RuntimeException(e);
        }
        return dom;
    }

    public static List<Element> parseData(InputStream input, String encoding) throws XmlPullParserException, IOException
    {
        if (input == null || encoding == null)
            throw new NullPointerException();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(input, encoding);
        List<Element> dom = new ArrayList<Element>();
        parseDataImpl(parser, dom);
        return dom;
    }

    private static void parseDataImpl(XmlPullParser parser, List<Element> dom) throws XmlPullParserException, IOException
    {
        int eventType = parser.getEventType();
        Element curElement = null;
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            switch (eventType)
            {
                case XmlPullParser.START_TAG:
                    if (curElement == null)
                    {
                        curElement = new Element(parser.getName());
                        List<String[]> attrs = curElement.getAttributes();
                        int attrCount = parser.getAttributeCount();
                        for (int i = 0; i < attrCount; i++)
                        {
                            attrs.add(new String[] { parser.getAttributeName(i), parser.getAttributeValue(i) });
                        }
                        dom.add(curElement);
                    } else
                    {
                        parseDataImpl(parser, curElement.getChildren());
                        // 递归会以父节点的END_TAG事件结束，此时需要重置当前节点
                        curElement = null;
                    }
                    break;
                case XmlPullParser.TEXT:
                    if (curElement != null)
                    {
                        curElement.setText(parser.getText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (curElement == null)
                        return;
                    curElement = null;
                    break;
            }
            eventType = parser.next();
        }
    }

}
