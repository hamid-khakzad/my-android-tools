package cn.emagsoftware.xmlpull.simpledom;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>简单的xml元素类
 * 
 * @author Wendell
 * 
 */
public class Element
{

    private String                     text     = "";
    private Map<String, List<Element>> children = null;

    public Element(boolean keepChildrenOrder)
    {
        if (keepChildrenOrder)
            children = new LinkedHashMap<String, List<Element>>();
        else
            children = new HashMap<String, List<Element>>();
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

    public Map<String, List<Element>> getChildren()
    {
        return children;
    }

    /**
     * <p>当前类的一个简单原则是对Children的操作要求通过getChildren()得到Map进行处理，但添加操作在代码编写上显示累赘，故提供此方法
     * 
     * @param tagName
     * @param curChildren
     * @return
     */
    public Element addChildren(String tagName, List<Element> curChildren)
    {
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

    /**
     * <p>当前类的一个简单原则是对Children的操作要求通过getChildren()得到Map进行处理，但添加操作在代码编写上显示累赘，故提供此方法
     * 
     * @param tagName
     * @param curChild
     * @return
     */
    public Element addChild(String tagName, Element curChild)
    {
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

    public boolean isLeaf()
    {
        return children.size() == 0;
    }

}
