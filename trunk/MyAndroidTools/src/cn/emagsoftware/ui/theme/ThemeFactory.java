package cn.emagsoftware.ui.theme;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ThemeFactory implements LayoutInflater.Factory
{

    private static String[]                          ANDROID_VIEW_FULLNAME_PREFIX = { "android.widget.", "android.webkit.", "android.view.", null };
    private static ThemeFactory                      factory                      = null;

    private Context                                  context                      = null;
    private String                                   packageName                  = null;
    private String                                   generalThemeName             = null;
    private Resources                                packageRes                   = null;
    private HashMap<String, HashMap<String, String>> stylesMap                    = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, String>                  generalThemeMap              = null;

    private boolean                                  shouldApplyTheme             = true;

    /**
     * <p>创建或修改ThemeFactory实例，当前类是单例的
     * 
     * @param context
     * @param packageName 需要应用的主题包名，如果要使用默认主题，可传null
     * @param generalThemeName 主题包styles.xml中通用主题样式的名字，如果没有通用主题样式，可传null
     * @return
     */
    public static ThemeFactory createOrUpdateInstance(Context context, String packageName, String generalThemeName)
    {
        if (factory == null)
            factory = new ThemeFactory(context);
        factory.update(packageName, generalThemeName);
        return factory;
    }

    private ThemeFactory(Context context)
    {
        if (context == null)
            throw new NullPointerException();
        this.context = context;
    }

    private void update(String packageName, String generalThemeName)
    {
        if (this.packageName == null && packageName == null)
            return; // 相同设置将直接返回
        if (this.packageName != null && this.packageName.equals(packageName))
        {
            if (this.generalThemeName == null && generalThemeName == null)
                return; // 相同设置将直接返回
            if (this.generalThemeName != null && this.generalThemeName.equals(generalThemeName))
                return; // 相同设置将直接返回
        }
        this.packageName = packageName;
        this.generalThemeName = generalThemeName;
        this.packageRes = null;
        this.stylesMap.clear();
        this.generalThemeMap = null;
        try
        {
            loadStyles();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void loadStyles() throws NameNotFoundException, XmlPullParserException, IOException
    {
        if (packageName == null)
            return; // 使用默认主题时将不需要加载样式
        PackageManager pm = context.getPackageManager();
        packageRes = pm.getResourcesForApplication(packageName);
        // styles.xml必须存放于xml文件夹下面，放在values文件夹下面将获取不到
        int stylesId = packageRes.getIdentifier("styles", "xml", packageName);
        if (stylesId > 0)
        {
            XmlResourceParser parser = packageRes.getXml(stylesId);
            int eventType = parser.getEventType();
            HashMap<String, String> style = null;
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                switch (eventType)
                {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String tagName = parser.getName();
                        if (tagName.equals("style"))
                        {
                            style = new HashMap<String, String>();
                            stylesMap.put(parser.getAttributeValue(null, "name"), style);
                        } else if (tagName.equals("item"))
                        {
                            style.put(parser.getAttributeValue(null, "name"), parser.nextText());
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
            if (generalThemeName != null)
            {
                this.generalThemeMap = stylesMap.get(generalThemeName);
            }
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs)
    {
        // TODO Auto-generated method stub
        if (shouldApplyTheme)
        {
            if (packageName == null)
                return null; // 返回null时系统会自己创建默认样式的View
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = null;
            for (int i = 0; i < ANDROID_VIEW_FULLNAME_PREFIX.length; i++)
            {
                try
                {
                    // 如果始终无法实例化view，ANDROID_VIEW_FULLNAME_PREFIX最后将认为是自定义view而使用null
                    view = inflater.createView(name, ANDROID_VIEW_FULLNAME_PREFIX[i], attrs);
                    break;
                } catch (ClassNotFoundException e)
                {
                    continue;
                }
            }
            if (view == null)
                return null;
            applyTheme(view); // 应用通用主题样式
            applyThemeFromLayout(view, attrs); // 应用布局文件中的样式
            return view;
        }
        return null;
    }

    void setApplyTheme(boolean shouldApplyTheme)
    {
        this.shouldApplyTheme = shouldApplyTheme;
    }

    boolean getApplyTheme()
    {
        return shouldApplyTheme;
    }

    private void applyTheme(View view)
    {
        if (generalThemeMap == null)
            return; // 如果未给定通用主题样式的名字或给定的名字无效，将返回
        Class<?> viewClass = view.getClass();
        while (true)
        {
            if (viewClass == null)
                break;
            String pName = viewClass.getPackage().getName();
            pName = pName.concat(".");
            boolean isOK = false;
            for (int i = 0; i < ANDROID_VIEW_FULLNAME_PREFIX.length; i++)
            {
                if (pName.equals(ANDROID_VIEW_FULLNAME_PREFIX[i]))
                {
                    isOK = true;
                    break;
                }
            }
            if (isOK)
                break;
            viewClass = viewClass.getSuperclass();
        }
        if (viewClass != null)
        {
            String className = viewClass.getSimpleName();
            char firstChar = className.charAt(0);
            className = className.replace(firstChar, Character.toLowerCase(firstChar));
            String itemName = "android:".concat(className).concat("Style");
            String itemValue = generalThemeMap.get(itemName);
            if (itemValue != null)
            {
                if (itemValue.startsWith("@style/"))
                {
                    String style = itemValue.substring("@style/".length());
                    applyStyle(view, style);
                }
            }
        }
    }

    private void applyThemeFromLayout(View view, AttributeSet paramAttributeSet)
    {
        int count = paramAttributeSet.getAttributeCount();
        for (int p = 0; p < count; p++)
        {
            String name = paramAttributeSet.getAttributeName(p);
            String value = paramAttributeSet.getAttributeValue(p);
            if (value.startsWith("?"))
            {
                if (generalThemeMap == null)
                    continue; // 如果未给定通用主题样式的名字或给定的名字无效，将忽略
                String key = null;
                if (value.startsWith("?attr/"))
                    key = value.substring("?attr/".length());
                else
                    key = value.substring(1);
                value = generalThemeMap.get(key);
                if (value == null)
                    continue;
            }
            if (value.startsWith("@style/"))
            {
                String style = value.substring("@style/".length());
                applyStyle(view, style);
            } else if (value.startsWith("@"))
            {
                int resId = 0;
                try
                {
                    resId = Integer.valueOf(value.substring(1)).intValue();
                } catch (NumberFormatException e)
                {
                }
                if (resId > 0)
                {
                    if (name.equals("background"))
                    {
                        Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                        if (d != null)
                        {
                            int i = view.getPaddingLeft();
                            int j = view.getPaddingTop();
                            int k = view.getPaddingRight();
                            int m = view.getPaddingBottom();
                            view.setBackgroundDrawable(d);
                            view.setPadding(i, j, k, m);
                        }
                    } else if (name.equals("padding"))
                    {
                        int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                        if (dimen >= 0)
                        {
                            view.setPadding(dimen, dimen, dimen, dimen);
                        }
                    } else if (name.equals("paddingLeft"))
                    {
                        int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                        if (dimen >= 0)
                        {
                            view.setPadding(dimen, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                        }
                    } else if (name.equals("paddingTop"))
                    {
                        int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                        if (dimen >= 0)
                        {
                            view.setPadding(view.getPaddingLeft(), dimen, view.getPaddingRight(), view.getPaddingBottom());
                        }
                    } else if (name.equals("paddingRight"))
                    {
                        int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                        if (dimen >= 0)
                        {
                            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), dimen, view.getPaddingBottom());
                        }
                    } else if (name.equals("paddingBottom"))
                    {
                        int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                        if (dimen >= 0)
                        {
                            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), dimen);
                        }
                    } else if (name.equals("textColor"))
                    {
                        if (view instanceof TextView)
                        {
                            ColorStateList c = getPackageColor(context.getResources().getResourceEntryName(resId));
                            if (c != null)
                            {
                                ((TextView) view).setTextColor(c);
                            }
                        }
                    } else if (name.equals("divider"))
                    {
                        if (view instanceof ListView)
                        {
                            Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                            if (d != null)
                            {
                                ((ListView) view).setDivider(d);
                            }
                        }
                    } else if (name.equals("groupIndicator"))
                    {
                        if (view instanceof ExpandableListView)
                        {
                            Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                            if (d != null)
                            {
                                ((ExpandableListView) view).setGroupIndicator(d);
                            }
                        }
                    } else if (name.equals("childDivider"))
                    {
                        if (view instanceof ExpandableListView)
                        {
                            Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                            if (d != null)
                            {
                                ((ExpandableListView) view).setChildDivider(d);
                            }
                        }
                    } else if (name.equals("src"))
                    {
                        if (view instanceof ImageView)
                        {
                            Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                            if (d != null)
                            {
                                ((ImageView) view).setImageDrawable(d);
                            }
                        }
                    } else if (name.equals("progressDrawable"))
                    {
                        if (view instanceof ProgressBar)
                        {
                            Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                            if (d != null)
                            {
                                ((ProgressBar) view).setProgressDrawable(d);
                            }
                        }
                    } else if (name.equals("listSelector"))
                    {
                        if (view instanceof AbsListView)
                        {
                            Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                            if (d != null)
                            {
                                ((AbsListView) view).setSelector(d);
                            }
                        }
                    } else if (name.equals("cacheColorHint"))
                    {
                        if (view instanceof AbsListView)
                        {
                            ColorStateList c = getPackageColor(context.getResources().getResourceEntryName(resId));
                            if (c != null)
                            {
                                ((AbsListView) view).setCacheColorHint(c.getDefaultColor());
                            }
                        }
                    } else if (name.equals("drawableRight"))
                    {
                        if (view instanceof TextView)
                        {
                            Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                            if (d != null)
                            {
                                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                            }
                        }
                    }
                }
            }
        }
    }

    private void applyStyle(View view, String styleName)
    {
        HashMap<String, String> style = stylesMap.get(styleName);
        if (style != null)
        {
            String value = style.get("android:background");
            if (value != null)
            {
                if (value.startsWith("@drawable/"))
                {
                    value = value.substring("@drawable/".length());
                    Drawable d = getPackageDrawable(value);
                    if (d != null)
                    {
                        int i = view.getPaddingLeft();
                        int j = view.getPaddingTop();
                        int k = view.getPaddingRight();
                        int m = view.getPaddingBottom();
                        view.setBackgroundDrawable(d);
                        view.setPadding(i, j, k, m);
                    }
                } else if (value.startsWith("#"))
                {
                    int color = Color.parseColor(value);
                    ColorDrawable cd = new ColorDrawable(color);
                    int i = view.getPaddingLeft();
                    int j = view.getPaddingTop();
                    int k = view.getPaddingRight();
                    int m = view.getPaddingBottom();
                    view.setBackgroundDrawable(cd);
                    view.setPadding(i, j, k, m);
                }
            }
            value = style.get("android:padding");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(d, d, d, d);
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex));
                        } catch (NumberFormatException e)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(d, d, d, d);
                    }
                }
            }
            value = style.get("android:paddingLeft");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(d, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex));
                        } catch (NumberFormatException e)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(d, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                    }
                }
            }
            value = style.get("android:paddingTop");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), d, view.getPaddingRight(), view.getPaddingBottom());
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex));
                        } catch (NumberFormatException e)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), d, view.getPaddingRight(), view.getPaddingBottom());
                    }
                }
            }
            value = style.get("android:paddingRight");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), d, view.getPaddingBottom());
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex));
                        } catch (NumberFormatException e)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), d, view.getPaddingBottom());
                    }
                }
            }
            value = style.get("android:paddingBottom");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), d);
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex));
                            final float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5f);
                        } catch (NumberFormatException e)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex));
                        } catch (NumberFormatException e)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), d);
                    }
                }
            }
            if (view instanceof TextView)
            {
                TextView textView = (TextView) view;
                value = style.get("android:textColor");
                if (value != null)
                {
                    if (value.startsWith("#"))
                    {
                        textView.setTextColor(Color.parseColor(value));
                    } else if (value.startsWith("@color/"))
                    {
                        int k = "@color/".length();
                        value = value.substring(k);
                        ColorStateList c = getPackageColor(value);
                        if (c != null)
                            textView.setTextColor(c);
                    }
                }
                value = style.get("android:textSize");
                if (value != null)
                {
                    if (value.indexOf("sp") != -1)
                    {
                        float size = Float.valueOf(value.replace("sp", "")).floatValue();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
                    } else if (value.indexOf("dip") != -1)
                    {
                        float size = Float.valueOf(value.replace("dip", "")).floatValue();
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                    }
                }
                value = style.get("android:textColorHighlight");
                if (value != null)
                {
                    if (value.startsWith("#"))
                    {
                        textView.setHighlightColor(Color.parseColor(value));
                    }
                }
                if (textView instanceof CompoundButton)
                {
                    CompoundButton cb = (CompoundButton) textView;
                    value = style.get("android:button");
                    if (value != null)
                    {
                        if (value.startsWith("@drawable/"))
                        {
                            value = value.substring("@drawable/".length());
                            Drawable d = getPackageDrawable(value);
                            if (d != null)
                                cb.setButtonDrawable(d);
                        }
                    }
                }
            } else if (view instanceof ListView)
            {
                ListView lv = (ListView) view;
                value = style.get("android:divider");
                if (value != null)
                {
                    if (value.startsWith("@drawable/"))
                    {
                        value = value.substring("@drawable/".length());
                        Drawable d = getPackageDrawable(value);
                        if (d != null)
                            lv.setDivider(d);
                    }
                }
                value = style.get("android:listSelector");
                if (value != null)
                {
                    if (value.startsWith("@drawable/"))
                    {
                        value = value.substring("@drawable/".length());
                        Drawable d = getPackageDrawable(value);
                        if (d != null)
                            lv.setSelector(d);
                    }
                }
                value = style.get("android:cacheColorHint");
                if (value != null)
                {
                    if (value.startsWith("@color/"))
                    {
                        value = value.substring("@color/".length());
                        ColorStateList c = getPackageColor(value);
                        if (c != null)
                            lv.setCacheColorHint(c.getDefaultColor());
                    }
                }
            }
        }
    }

    private Drawable getPackageDrawable(String drawableName)
    {
        if (packageName == null)
            return null;
        int drawableId = packageRes.getIdentifier(drawableName, "drawable", packageName);
        if (drawableId > 0)
        {
            return packageRes.getDrawable(drawableId);
        }
        return null;
    }

    private int getPackageDimensionPixelSize(String dimensionName)
    {
        if (packageName == null)
            return -1;
        int dimenId = packageRes.getIdentifier(dimensionName, "dimen", packageName);
        if (dimenId > 0)
        {
            return packageRes.getDimensionPixelSize(dimenId);
        }
        return -1;
    }

    private ColorStateList getPackageColor(String colorName)
    {
        if (packageName == null)
            return null;
        int colorId = packageRes.getIdentifier(colorName, "color", packageName);
        if (colorId > 0)
        {
            return packageRes.getColorStateList(colorId);
        }
        return null;
    }

}
