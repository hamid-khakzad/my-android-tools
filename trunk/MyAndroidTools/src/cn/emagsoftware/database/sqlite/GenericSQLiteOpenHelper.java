package cn.emagsoftware.database.sqlite;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * generic sqlite open helper
 * 
 * @author Wendell
 * @version 1.5
 */
public abstract class GenericSQLiteOpenHelper extends SQLiteOpenHelper
{

    /**
     * <p>一般子类实现需要采用单例模式，原因如下：</>
     * <p>1.SQLite不允许并发操作，并发操作会抛出异常，当前类的数据库操作做了同步，通过单例调用，能够避免异常</>
     * <p>2.通过setWriteAheadLoggingEnabled(true)开启WAL模式后，单例同样能够做到多并发</>
     * <p>3.单例通常不需要关闭数据库，从而提高了效率</>
     * @param context
     * @param dbName
     * @param version
     */
    public GenericSQLiteOpenHelper(Context context, String dbName, int version)
    {
        super(context, dbName, null, version);
    }

    /**
     * 一些必需或可选实现的方法 <p>onCreate:创建指定的数据库时执行，该方法只会被执行一次，在软件更新时由于会保留原有数据，故该方法将不再被调用，除非卸载软件后再安装 <p>onUpgrade:指定的数据库版本号发生变化时执行，执行时会传入原来的版本号和新版本号，方便数据库的更新 <p>onOpen:每次成功打开指定的数据库时首先被执行
     */

    /**
     * <p>查询SQL
     * 
     * @param sql
     * @param selectionArgs
     * @return
     */
    public List<Map<String, String>> rawQuery(String sql, String[] selectionArgs)
    {
        return rawQuery(getReadableDatabase(),sql,selectionArgs);
    }

    public static List<Map<String, String>> rawQuery(SQLiteDatabase db, String sql, String[] selectionArgs) {
        List<Map<String, String>> returnVal = new LinkedList<Map<String, String>>();
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, selectionArgs);
            int columnCount = cursor.getColumnCount();
            while (true)
            {
                if (!cursor.moveToNext())
                    return returnVal;
                Map<String, String> row = new HashMap<String, String>();
                for (int i = 0; i < columnCount; i++)
                {
                    String name = cursor.getColumnName(i);
                    String value = cursor.getString(i);
                    row.put(name, value);
                }
                returnVal.add(row);
            }
        } finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    /**
     * <p>查询SQL
     * 
     * @param sql
     * @param selectionArgs
     * @return
     */
    public Map<String, String> rawQueryForFirstRow(String sql, String[] selectionArgs)
    {
        return rawQueryForFirstRow(getReadableDatabase(),sql,selectionArgs);
    }

    public static Map<String, String> rawQueryForFirstRow(SQLiteDatabase db, String sql, String[] selectionArgs) {
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, selectionArgs);
            if (!cursor.moveToNext())
                return null;
            int columnCount = cursor.getColumnCount();
            Map<String, String> row = new HashMap<String, String>();
            for (int i = 0; i < columnCount; i++)
            {
                String name = cursor.getColumnName(i);
                String value = cursor.getString(i);
                row.put(name, value);
            }
            return row;
        } finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    /**
     * <p>查询SQL
     * 
     * @param sql
     * @param selectionArgs
     * @return
     */
    public List<String> rawQueryForFirstField(String sql, String[] selectionArgs)
    {
        return rawQueryForFirstField(getReadableDatabase(),sql,selectionArgs);
    }

    public static List<String> rawQueryForFirstField(SQLiteDatabase db, String sql, String[] selectionArgs) {
        List<String> returnVal = new LinkedList<String>();
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, selectionArgs);
            while (true)
            {
                if (!cursor.moveToNext())
                    return returnVal;
                String value = cursor.getString(0);
                returnVal.add(value);
            }
        } finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    /**
     * <p>执行SQL
     * 
     * @param sql
     */
    public void execSQL(String sql)
    {
        execSQL(getWritableDatabase(),sql);
    }

    public static void execSQL(SQLiteDatabase db, String sql) {
        db.execSQL(sql);
    }

    /**
     * <p>执行SQL
     * 
     * @param sql
     * @param bindArgs
     */
    public void execSQL(String sql, Object[] bindArgs)
    {
        execSQL(getWritableDatabase(),sql,bindArgs);
    }

    public static void execSQL(SQLiteDatabase db, String sql, Object[] bindArgs) {
        db.execSQL(sql, bindArgs);
    }

}
