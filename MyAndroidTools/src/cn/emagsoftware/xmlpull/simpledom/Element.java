package cn.emagsoftware.xmlpull.simpledom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParserException;

/**
 * <p>�򵥵�xmlԪ���� <p>��ǰ���һЩ������ͻʹ��XmlPullParserException�׳�����Ϊ�˷����ⲿ��xml�����ͳһ����������ڳ�����©
 * 
 * @author Wendell
 * 
 */
public class Element
{

    private boolean                    isLeaf   = false;
    private String                     text     = "";
    private Map<String, List<Element>> children = null;

    public Element(boolean isLeaf, boolean keepOrder)
    {
        this.isLeaf = isLeaf;
        if (keepOrder)
            children = new LinkedHashMap<String, List<Element>>();
        else
            children = new HashMap<String, List<Element>>();
    }

    public String getText() throws XmlPullParserException
    {
        if (!isLeaf)
            throw new XmlPullParserException("only leaf element can get text!");
        return text;
    }

    public Element setText(String text) throws XmlPullParserException
    {
        if (!isLeaf)
            throw new XmlPullParserException("only leaf element can set text!");
        if (text == null)
            throw new NullPointerException();
        this.text = text;
        return this;
    }

    public Map<String, List<Element>> getChildren() throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not get children!");
        return children;
    }

    public List<Element> getChildren(String tagName) throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not get children!");
        if (tagName == null)
            throw new NullPointerException();
        return children.get(tagName);
    }

    public Element addChildren(String tagName, List<Element> curChildren) throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not add children!");
        if (tagName == null || curChildren == null)
            throw new NullPointerException();
        List<Element> tagChildren = children.get(tagName);
        if (tagChildren == null)
        {
            tagChildren = new LinkedList<Element>();
            children.put(tagName, tagChildren);
        }
        tagChildren.addAll(curChildren);
        return this;
    }

    public Element addChild(String tagName, Element curChild) throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not add child!");
        if (tagName == null || curChild == null)
            throw new NullPointerException();
        List<Element> tagChildren = children.get(tagName);
        if (tagChildren == null)
        {
            tagChildren = new LinkedList<Element>();
            children.put(tagName, tagChildren);
        }
        tagChildren.add(curChild);
        return this;
    }

    /**
     * @deprecated ֱ��ʹ�ø�Map���滻Children��˼·�Ѿ�ǿ�Ҳ�����ʹ��
     * @param children
     * @throws XmlPullParserException
     */
    public Element setChildren(Map<String, List<Element>> children) throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not set children!");
        if (children == null)
            throw new NullPointerException();
        this.children = children;
        return this;
    }

    /**
     * @deprecated �÷���һ��ΪsetChildren(Map<String, List<Element>>)�ṩ��ݵ��ã�����setChildren�����Ѳ�����ʹ�ã��ʽ���ǰ����Ҳ���Ϊ��ʱ
     * @param dom
     * @param keepOrder
     * @return
     */
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

    public boolean isLeaf()
    {
        return isLeaf;
    }

}
