package cn.emagsoftware.xmlpull.simpledom;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

/**
 * <p>简单的xml元素类 <p>当前类的一些条件冲突使用XmlPullParserException抛出，是为了方便外部对xml错误的统一捕获而不至于出现遗漏
 * 
 * @author Wendell
 * 
 */
public class Element
{

    private boolean                    isLeaf   = false;
    private String                     text     = "";
    private Map<String, List<Element>> children = null;

    /**
     * @deprecated 已使用 Element(isLeaf,iskeepOrder)代替
     * @param isLeaf
     */
    public Element(boolean isLeaf)
    {
        this.isLeaf = isLeaf;
        children = new HashMap<String, List<Element>>();
    }

    public Element(boolean isLeaf, boolean iskeepOrder)
    {
        this.isLeaf = isLeaf;
        if (iskeepOrder)
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

    public List<Element> getChildren(String tagName) throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not get children!");
        if (tagName == null)
            throw new NullPointerException();
        return children.get(tagName);
    }

    public void addChildren(String tagName, List<Element> curChildren) throws XmlPullParserException
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
    }

    public void addChild(String tagName, Element curChild) throws XmlPullParserException
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
    }

    /**
     * @deprecated 直接使用根Map来替换Children的思路已经强烈不建议使用
     * @param children
     * @throws XmlPullParserException
     */
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
