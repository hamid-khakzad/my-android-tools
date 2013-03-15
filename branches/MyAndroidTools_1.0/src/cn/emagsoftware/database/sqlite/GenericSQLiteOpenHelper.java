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
 * @version 1.1
 */
public abstract class GenericSQLiteOpenHelper extends SQLiteOpenHelper
{

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
     * @param closeDB 是否关闭数据库。出于效率考虑，应用程序可能会通过缓存当前实例的方式来缓存当前数据库，此时对当前数据库的操作完成后不应该关闭数据库
     * @return
     */
    public List<Map<String, String>> rawQuery(String sql, String[] selectionArgs, boolean closeDB)
    {
        List<Map<String, String>> returnVal = new LinkedList<Map<String, String>>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try
        {
            db = getReadableDatabase();
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
                    if (value == null)
                        value = "";
                    row.put(name, value);
                }
                returnVal.add(row);
            }
        } finally
        {
            try
            {
                if (cursor != null)
                    cursor.close();
            } finally
            {
                if (db != null && closeDB)
                    db.close();
            }
        }
    }

    /**
     * <p>查询SQL
     * 
     * @param sql
     * @param selectionArgs
     * @param closeDB 是否关闭数据库。出于效率考虑，应用程序可能会通过缓存当前实例的方式来缓存当前数据库，此时对当前数据库的操作完成后不应该关闭数据库
     * @return
     */
    public Map<String, String> rawQueryForFirstRow(String sql, String[] selectionArgs, boolean closeDB)
    {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery(sql, selectionArgs);
            if (!cursor.moveToNext())
                return null;
            int columnCount = cursor.getColumnCount();
            Map<String, String> row = new HashMap<String, String>();
            for (int i = 0; i < columnCount; i++)
            {
                String name = cursor.getColumnName(i);
                String value = cursor.getString(i);
                if (value == null)
                    value = "";
                row.put(name, value);
            }
            return row;
        } finally
        {
            try
            {
                if (cursor != null)
                    cursor.close();
            } finally
            {
                if (db != null && closeDB)
                    db.close();
            }
        }
    }

    /**
     * <p>查询SQL
     * 
     * @param sql
     * @param selectionArgs
     * @param closeDB 是否关闭数据库。出于效率考虑，应用程序可能会通过缓存当前实例的方式来缓存当前数据库，此时对当前数据库的操作完成后不应该关闭数据库
     * @return
     */
    public List<String> rawQueryForFirstField(String sql, String[] selectionArgs, boolean closeDB)
    {
        List<String> returnVal = new LinkedList<String>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery(sql, selectionArgs);
            while (true)
            {
                if (!cursor.moveToNext())
                    return returnVal;
                String value = cursor.getString(0);
                if (value == null)
                    value = "";
                returnVal.add(value);
            }
        } finally
        {
            try
            {
                if (cursor != null)
                    cursor.close();
            } finally
            {
                if (db != null && closeDB)
                    db.close();
            }
        }
    }

    /**
     * <p>执行SQL
     * 
     * @param sql
     * @param closeDB 是否关闭数据库。出于效率考虑，应用程序可能会通过缓存当前实例的方式来缓存当前数据库，此时对当前数据库的操作完成后不应该关闭数据库
     */
    public void execSQL(String sql, boolean closeDB)
    {
        SQLiteDatabase db = null;
        try
        {
            db = getWritableDatabase();
            db.execSQL(sql);
        } finally
        {
            if (db != null && closeDB)
                db.close();
        }
    }

    /**
     * <p>执行SQL
     * 
     * @param sql
     * @param bindArgs
     * @param closeDB 是否关闭数据库。出于效率考虑，应用程序可能会通过缓存当前实例的方式来缓存当前数据库，此时对当前数据库的操作完成后不应该关闭数据库
     */
    public void execSQL(String sql, Object[] bindArgs, boolean closeDB)
    {
        SQLiteDatabase db = null;
        try
        {
            db = getWritableDatabase();
            db.execSQL(sql, bindArgs);
        } finally
        {
            if (db != null && closeDB)
                db.close();
        }
    }

}
