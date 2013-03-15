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
     * һЩ������ѡʵ�ֵķ��� <p>onCreate:����ָ�������ݿ�ʱִ�У��÷���ֻ�ᱻִ��һ�Σ����������ʱ���ڻᱣ��ԭ�����ݣ��ʸ÷��������ٱ����ã�����ж��������ٰ�װ <p>onUpgrade:ָ�������ݿ�汾�ŷ����仯ʱִ�У�ִ��ʱ�ᴫ��ԭ���İ汾�ź��°汾�ţ��������ݿ�ĸ��� <p>onOpen:ÿ�γɹ���ָ�������ݿ�ʱ���ȱ�ִ��
     */

    /**
     * <p>��ѯSQL
     * 
     * @param sql
     * @param selectionArgs
     * @param closeDB �Ƿ�ر����ݿ⡣����Ч�ʿ��ǣ�Ӧ�ó�����ܻ�ͨ�����浱ǰʵ���ķ�ʽ�����浱ǰ���ݿ⣬��ʱ�Ե�ǰ���ݿ�Ĳ�����ɺ�Ӧ�ùر����ݿ�
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
     * <p>��ѯSQL
     * 
     * @param sql
     * @param selectionArgs
     * @param closeDB �Ƿ�ر����ݿ⡣����Ч�ʿ��ǣ�Ӧ�ó�����ܻ�ͨ�����浱ǰʵ���ķ�ʽ�����浱ǰ���ݿ⣬��ʱ�Ե�ǰ���ݿ�Ĳ�����ɺ�Ӧ�ùر����ݿ�
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
     * <p>��ѯSQL
     * 
     * @param sql
     * @param selectionArgs
     * @param closeDB �Ƿ�ر����ݿ⡣����Ч�ʿ��ǣ�Ӧ�ó�����ܻ�ͨ�����浱ǰʵ���ķ�ʽ�����浱ǰ���ݿ⣬��ʱ�Ե�ǰ���ݿ�Ĳ�����ɺ�Ӧ�ùر����ݿ�
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
     * <p>ִ��SQL
     * 
     * @param sql
     * @param closeDB �Ƿ�ر����ݿ⡣����Ч�ʿ��ǣ�Ӧ�ó�����ܻ�ͨ�����浱ǰʵ���ķ�ʽ�����浱ǰ���ݿ⣬��ʱ�Ե�ǰ���ݿ�Ĳ�����ɺ�Ӧ�ùر����ݿ�
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
     * <p>ִ��SQL
     * 
     * @param sql
     * @param bindArgs
     * @param closeDB �Ƿ�ر����ݿ⡣����Ч�ʿ��ǣ�Ӧ�ó�����ܻ�ͨ�����浱ǰʵ���ķ�ʽ�����浱ǰ���ݿ⣬��ʱ�Ե�ǰ���ݿ�Ĳ�����ɺ�Ӧ�ùر����ݿ�
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
