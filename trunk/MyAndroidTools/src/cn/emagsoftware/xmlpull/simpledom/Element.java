package cn.emagsoftware.xmlpull.simpledom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import cn.emagsoftware.util.StringUtilities;

/**
 * <p>简单的xml元素类 <p>当前类的一些条件冲突使用XmlPullParserException抛出，是为了方便外部对xml错误的统一捕获而不至于出现遗漏
 * 
 * @author Wendell
 * 
 */
public class Element
{
    private String                     text              = null;
    private Map<String, Element>       singleTagChildren = null;
    private Map<String, List<Element>> children          = null;
    private boolean                    isLeaf            = false;

    public Element(boolean isLeaf)
    {
        this.isLeaf = isLeaf;
    }

    public String getText() throws XmlPullParserException
    {
        if (!isLeaf)
            throw new XmlPullParserException("only leaf element can get text!");
        return StringUtilities.toStringWhenNull(text, "");
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
        if (singleTagChildren != null)
            return SimpleDomManager.convertDom(singleTagChildren);
        if (children != null)
            return children;
        return new HashMap<String, List<Element>>();
    }

    public void setSingleTagChildren(Map<String, Element> children) throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not set children!");
        if (children == null)
            throw new NullPointerException();
        this.singleTagChildren = children;
        this.children = null;
    }

    public void setChildren(Map<String, List<Element>> children) throws XmlPullParserException
    {
        if (isLeaf)
            throw new XmlPullParserException("leaf element can not set children!");
        if (children == null)
            throw new NullPointerException();
        this.singleTagChildren = null;
        this.children = children;
    }

    public boolean isLeaf()
    {
        return isLeaf;
    }

}
