package cn.emagsoftware.xmlpull.simpledom;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>�򵥵�xmlԪ����
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
     * <p>��ǰ���һ����ԭ���Ƕ�Children�Ĳ���Ҫ��ͨ��getChildren()�õ�Map���д�������Ӳ����ڴ����д����ʾ��׸�����ṩ�˷���
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
     * <p>��ǰ���һ����ԭ���Ƕ�Children�Ĳ���Ҫ��ͨ��getChildren()�õ�Map���д�������Ӳ����ڴ����д����ʾ��׸�����ṩ�˷���
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
