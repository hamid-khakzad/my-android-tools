package cn.emagsoftware.xmlpull.simpledom;

import java.util.ArrayList;
import java.util.HashMap;
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
     * <p>��ǰ���һ����ԭ���Ƕ�Attributes������ɾ���ġ������Ҫ��ͨ��getAttributes()�õ�List���д��� <p>��Attributes�Ĳ��Ҳ�������getAttributes(String attrName)���Ϸ�������Ӧ�ṩ��ط������������Ǳ������ң����ܻ����ϲ㲻�������ʵ�ֵ����������Ƶ�����õ���Ч�ʽ��ͣ����Խ����ϲ�ʵ��
     * 
     * @return
     */
    public List<String[]> getAttributes()
    {
        return attributes;
    }

    public Map<String,String> attributesToSimpleBean()
    {
        Map<String,String> returnVal = new HashMap<String, String>();
        for(String[] attribute:attributes)
        {
            returnVal.put(attribute[0],attribute[1]);
        }
        return returnVal;
    }

    /**
     * <p>��ǰ���һ����ԭ���Ƕ�Children������ɾ���ġ������Ҫ��ͨ��getChildren()�õ�List���д��� <p>��Children�Ĳ��Ҳ�������getChildren(String tag)���Ϸ�������Ӧ�ṩ��ط������������Ǳ������ң����ܻ����ϲ㲻�������ʵ�ֵ����������Ƶ�����õ���Ч�ʽ��ͣ����Խ����ϲ�ʵ��
     * 
     * @return
     */
    public List<Element> getChildren()
    {
        return children;
    }

    public Map<String,Element> childrenToSimpleBean()
    {
        Map<String,Element> returnVal = new HashMap<String, Element>();
        for(Element element:children)
        {
            returnVal.put(element.getTag(),element);
        }
        return returnVal;
    }

    public boolean isLeaf()
    {
        return children.size() == 0;
    }

}
