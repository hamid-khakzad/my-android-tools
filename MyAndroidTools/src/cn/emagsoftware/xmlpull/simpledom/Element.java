package cn.emagsoftware.xmlpull.simpledom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Element
{
    private String                     text     = "";
    private Map<String, List<Element>> children = new HashMap<String, List<Element>>();
    private boolean                    isLeaf   = false;

    public Element(boolean isLeaf)
    {
        this.isLeaf = isLeaf;
    }

    public String getText()
    {
        if (!isLeaf)
            throw new IllegalStateException("only leaf element can get text!");
        return text;
    }

    public void setText(String text)
    {
        if (!isLeaf)
            throw new IllegalStateException("only leaf element can set text!");
        if (text == null)
            throw new NullPointerException();
        this.text = text;
    }

    public Map<String, List<Element>> getChildren()
    {
        if (isLeaf)
            throw new IllegalStateException("leaf element can not get children!");
        return children;
    }

    public void setSingleTagChildren(Map<String, Element> children)
    {
        setChildren(SimpleDomManager.convertDom(children));
    }

    public void setChildren(Map<String, List<Element>> children)
    {
        if (isLeaf)
            throw new IllegalStateException("leaf element can not set children!");
        if (children == null)
            throw new NullPointerException();
        this.children = children;
    }

    public boolean isLeaf()
    {
        return isLeaf;
    }

}
