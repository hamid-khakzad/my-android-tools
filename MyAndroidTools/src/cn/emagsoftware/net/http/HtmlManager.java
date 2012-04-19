package cn.emagsoftware.net.http;

public final class HtmlManager
{

    private HtmlManager()
    {
    }

    public static String removeComment(String html)
    {
        String start = "<!--";
        String end = "-->";
        String[] cut1 = html.split(start);
        StringBuffer sb = new StringBuffer();
        for (String temp : cut1)
        {
            int index = temp.indexOf(end);
            if (index >= 0 && index + end.length() < temp.length())
            {
                sb.append(temp.substring(index + end.length()));
            } else
            {
                sb.append(temp);
            }
        }
        return sb.toString();
    }

}
