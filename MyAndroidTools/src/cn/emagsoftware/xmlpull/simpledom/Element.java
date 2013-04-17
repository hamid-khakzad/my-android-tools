package cn.emagsoftware.xmlpull.simpledom;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>�򵥵�xmlԪ����
 * 
 * @author Wendell
 * 
 */
public class Element
{

    private String         text       = "";
    private List<String[]> attributes = new ArrayList<String[]>();
    private List<Element>  children   = new ArrayList<Element>();

    public Element()
    {
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

    /**
     * <p>��ǰ���һ����ԭ���Ƕ�Children������ɾ���ġ������Ҫ��ͨ��getChildren()�õ�List���д��� <p>��Children�Ĳ��Ҳ�������getChildren(String tagName)���Ϸ�������Ӧ�ṩ��ط������������Ǳ������ң����ܻ����ϲ㲻�������ʵ�ֵ����������Ƶ�����õ���Ч�ʽ��ͣ����Խ����ϲ�ʵ��
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
