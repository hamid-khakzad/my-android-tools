package cn.emagsoftware.xmlpull.simpledom;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>简单的xml元素类
 * 
 * @author Wendell
 * 
 */
public class Element
{

    private String         tag        = "";
    private String         text       = "";
    private List<String[]> attributes = new ArrayList<String[]>();
    private List<Element>  children   = new ArrayList<Element>();

    public Element(String tag)
    {
        if (tag == null)
            throw new NullPointerException();
        this.tag = tag;
    }

    public String getTag()
    {
        return tag;
    }

    public Element setTag(String tag)
    {
        if (tag == null)
            throw new NullPointerException();
        this.tag = tag;
        return this;
    }

    public String getText()
    {
        return text;
    }

    public Element setText(String text)
    {
        if (text == null)
            throw new NullPointerException();
        this.text = text;
        return this;
    }

    /**
     * <p>当前类的一个简单原则是对Attributes的增、删、改、查操作要求通过getAttributes()得到List进行处理 <p>对Attributes的查找操作（如getAttributes(String attrName)）较繁琐，理应提供相关方法，但由于是遍历查找，可能会在上层不清楚具体实现的情况下由于频繁调用导致效率降低，所以交由上层实现
     * 
     * @return
     */
    public List<String[]> getAttributes()
    {
        return attributes;
    }

    /**
     * <p>当前类的一个简单原则是对Children的增、删、改、查操作要求通过getChildren()得到List进行处理 <p>对Children的查找操作（如getChildren(String tag)）较繁琐，理应提供相关方法，但由于是遍历查找，可能会在上层不清楚具体实现的情况下由于频繁调用导致效率降低，所以交由上层实现
     * 
     * @return
     */
    public List<Element> getChildren()
    {
        return children;
    }

    public boolean isLeaf()
    {
        return children.size() == 0;
    }

}
