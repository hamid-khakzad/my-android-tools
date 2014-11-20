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
     * <p>һ������ʵ����Ҫ���õ���ģʽ��ԭ�����£�</>
     * <p>1.SQLite�������������������������׳��쳣����ǰ������ݿ��������ͬ����ͨ���������ã��ܹ������쳣</>
     * <p>2.ͨ��setWriteAheadLoggingEnabled(true)����WALģʽ�󣬵���ͬ���ܹ������ಢ��</>
     * <p>3.����ͨ������Ҫ�ر����ݿ⣬�Ӷ������Ч��</>
     * @param context
     * @param dbName
     * @param version
     */
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
     * <p>��ѯSQL
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
     * <p>��ѯSQL
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
     * <p>ִ��SQL
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
     * <p>ִ��SQL
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
