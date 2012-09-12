package cn.emagsoftware.xmlpull.simpledom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public final class SimpleDomManager
{

    private SimpleDomManager()
    {
    }

    public static String serializeDom(Map<String, List<Element>> dom, boolean containsXmlHead)
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String encoding = "utf-8";
        try
        {
            serializeDom(dom, containsXmlHead, output, encoding);
            return new String(output.toByteArray(), encoding);
        } catch (IOException e)
        {
            // ���л����ַ���һ�㲻�����IOException
            throw new RuntimeException(e);
        }
    }

    public static void serializeDom(Map<String, List<Element>> dom, boolean containsXmlHead, OutputStream output, String encoding) throws IOException
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

    private static void serializeDomImpl(XmlSerializer serializer, Map<String, List<Element>> dom) throws IOException
    {
        Iterator<Entry<String, List<Element>>> maps = dom.entrySet().iterator();
        while (maps.hasNext())
        {
            Entry<String, List<Element>> map = maps.next();
            String key = map.getKey();
            List<Element> value = map.getValue();
            if (key == null || value == null)
                continue;
            Iterator<Element> elements = value.iterator();
            while (elements.hasNext())
            {
                Element element = elements.next();
                if (element == null)
                    continue;
                serializer.startTag(null, key);
                if (element.isLeaf())
                {
                    String text = null;
                    try
                    {
                        text = element.getText();
                    } catch (XmlPullParserException e)
                    {
                        // ���쳣������֣���Ϊ�ⲿ�����жϣ��ʼ򵥴���֮
                        throw new RuntimeException(e);
                    }
                    serializer.text(text);
                } else
                {
                    Map<String, List<Element>> subDom = null;
                    try
                    {
                        subDom = element.getChildren();
                    } catch (XmlPullParserException e)
                    {
                        // ���쳣������֣���Ϊ�ⲿ�����жϣ��ʼ򵥴���֮
                        throw new RuntimeException(e);
                    }
                    serializeDomImpl(serializer, subDom);
                }
                serializer.endTag(null, key);
            }
        }
    }

    public static Map<String, List<Element>> convertDom(Map<String, Element> dom, boolean keepOrder)
    {
        if (dom == null)
            throw new NullPointerException();
        Iterator<Entry<String, Element>> maps = dom.entrySet().iterator();
        Map<String, List<Element>> newDom = null;
        if (keepOrder)
            newDom = new LinkedHashMap<String, List<Element>>();
        else
            newDom = new HashMap<String, List<Element>>();
        while (maps.hasNext())
        {
            Entry<String, Element> map = maps.next();
            String key = map.getKey();
            Element value = map.getValue();
            List<Element> list = new LinkedList<Element>();
            list.add(value);
            newDom.put(key, list);
        }
        return newDom;
    }

    public static Map<String, List<Element>> parseData(String data) throws XmlPullParserException
    {
        if (data == null)
            throw new NullPointerException();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(data));
        Map<String, List<Element>> dom = new HashMap<String, List<Element>>();
        try
        {
            parseDataImpl(parser, dom);
        } catch (IOException e)
        {
            // ���ַ�������һ�㲻�����IOException
            throw new RuntimeException(e);
        }
        return dom;
    }

    public static Map<String, List<Element>> parseData(InputStream input, String encoding) throws XmlPullParserException, IOException
    {
        if (input == null || encoding == null)
            throw new NullPointerException();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(input, encoding);
        Map<String, List<Element>> dom = new HashMap<String, List<Element>>();
        parseDataImpl(parser, dom);
        return dom;
    }

    private static void parseDataImpl(XmlPullParser parser, Map<String, List<Element>> dom) throws XmlPullParserException, IOException
    {
        int eventType = parser.getEventType();
        String curTag = null;
        boolean isCurTagPut = false;
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            boolean shouldNext = true;
            switch (eventType)
            {
                case XmlPullParser.START_TAG:
                    if (curTag == null)
                    {
                        curTag = parser.getName();
                    } else
                    {
                        Element element = new Element(false);
                        Map<String, List<Element>> childrenDom = new HashMap<String, List<Element>>();
                        element.setChildren(childrenDom);
                        List<Element> existedList = dom.get(curTag);
                        if (existedList == null)
                        {
                            existedList = new LinkedList<Element>();
                            dom.put(curTag, existedList);
                        }
                        existedList.add(element);
                        isCurTagPut = true;
                        parseDataImpl(parser, childrenDom);
                        // �ݹ���Ը��ڵ��END_TAG�¼���������ʱ��Ҫִ�и��ڵ��END_TAG�����Ա�֤�߼�һ���ԣ��ʲ���Խ��
                        eventType = parser.getEventType();
                        shouldNext = false;
                    }
                    break;
                case XmlPullParser.TEXT:
                    Element element = new Element(true);
                    element.setText(parser.getText());
                    List<Element> existedList = dom.get(curTag);
                    if (existedList == null)
                    {
                        existedList = new LinkedList<Element>();
                        dom.put(curTag, existedList);
                    }
                    existedList.add(element);
                    isCurTagPut = true;
                    break;
                case XmlPullParser.END_TAG:
                    if (curTag == null)
                        return;
                    if (!isCurTagPut)
                    {
                        existedList = dom.get(curTag);
                        if (existedList == null)
                        {
                            existedList = new LinkedList<Element>();
                            dom.put(curTag, existedList);
                        }
                        existedList.add(new Element(true));
                    }
                    curTag = null;
                    isCurTagPut = false;
                    break;
            }
            if (shouldNext)
                eventType = parser.next();
        }
    }

}