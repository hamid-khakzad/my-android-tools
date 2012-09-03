package cn.emagsoftware.xmlpull.simpledom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

/**
 * <p>�򵥵�xmlԪ���� <p>��ǰ���һЩ������ͻʹ��XmlPullParserException�׳�����Ϊ�˷����ⲿ��xml�����ͳһ����������ڳ�����©
 * 
 * @author Wendell
 * 
 */
public class Element
{
    private String                     text     = "";
    private Map<String, List<Element>> children = new HashMap<String, List<Element>>();
    private boolean                    isLeaf   = false;

    public Element(boolean isLeaf)
    {
        this.isLeaf = isLeaf;
    }

    public String getText() throws XmlPullParserException
    {
        if (!isLeaf)
            throw new XmlPullParserException("only leaf element can get text!");
        return text;
    }

    public void setText(String text) throws XmlPullParserException
    {
        if (!isLeaf)
            throw new XmlPullParserException("only leaf element can set text!");
        if (text == null)
            throw new NullPointerException();
        this.text = text;
    }

    public Map<String, List<Element>> getChildren() throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not get children!");
        return children;
    }

    public void setChildren(Map<String, List<Element>> children) throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not set children!");
        if (children == null)
            throw new NullPointerException();
        this.children = children;
    }

    public boolean isLeaf()
    {
        return isLeaf;
    }

}
