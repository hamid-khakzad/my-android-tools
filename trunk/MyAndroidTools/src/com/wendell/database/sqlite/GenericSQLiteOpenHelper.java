package com.wendell.database.sqlite;

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
 * @author Wendell
 * @version 1.0
 */
public abstract class GenericSQLiteOpenHelper extends SQLiteOpenHelper{
	
	public GenericSQLiteOpenHelper(Context context,String dbName,int version){
		super(context,dbName,null,version);
	}
	
	/**
	 * 一些需要实现的方法
	 * <p>onCreate:创建指定的数据库时执行，该方法只会被执行一次，在软件更新时由于会保留原有数据，故该方法将不再被调用，除非卸载软件后再安装
	 * <p>onUpgrade:指定的数据库版本号发生变化时执行，执行时会传入原来的版本号和新版本号，方便数据库的更新
	 * <p>onOpen:每次成功打开指定的数据库时首先被执行
	 * <p>举个例子，onCreate的实现可以是以下语句：<br>
	 * public void onCreate(SQLiteDatabase db) {
	 *   String sql = "CREATE TABLE IF NOT EXISTS T_USER (ID INTEGER PRIMARY KEY,NAME TEXT,PASSWORD TEXT);";
	 *   db.execSQL(sql);
	 * }
	 */
	
    /**
     * <p>查询SQL
     * @param sql
     * @param selectionArgs
     * @return
     */
    public List<Map<String,String>> rawQuery(String sql,String[] selectionArgs){
    	List<Map<String,String>> returnVal = new LinkedList<Map<String,String>>();
    	SQLiteDatabase db = null;
    	Cursor cursor = null;
    	try{
    		db = getReadableDatabase();
    		cursor = db.rawQuery(sql, selectionArgs);
    		int columnCount = cursor.getColumnCount();
    		while(true) {
    			if(!cursor.moveToNext()) return returnVal;
    			Map<String,String> row = new HashMap<String, String>();
    			for(int i = 0;i < columnCount;i++){
    				String name = cursor.getColumnName(i);
    				String value = cursor.getString(i);
    				if(value == null) value = "";
    				row.put(name, value);
    			}
    			returnVal.add(row);
    		}
    	}finally{
    		try{
    			if(cursor != null) cursor.close();
    		}finally{
    			if(db != null) db.close();
    		}
    	}
    }
    
    /**
     * <p>查询SQL
     * @param sql
     * @param selectionArgs
     * @return
     */
    public Map<String,String> rawQueryForFirstRow(String sql,String[] selectionArgs){
    	SQLiteDatabase db = null;
    	Cursor cursor = null;
    	try{
    		db = getReadableDatabase();
    		cursor = db.rawQuery(sql, selectionArgs);
    		if(!cursor.moveToNext()) return null;
    		int columnCount = cursor.getColumnCount();
			Map<String,String> row = new HashMap<String, String>();
			for(int i = 0;i < columnCount;i++){
				String name = cursor.getColumnName(i);
				String value = cursor.getString(i);
				if(value == null) value = "";
				row.put(name, value);
			}
			return row;
    	}finally{
    		try{
    			if(cursor != null) cursor.close();
    		}finally{
    			if(db != null) db.close();
    		}
    	}
    }
    
    /**
     * <p>查询SQL
     * @param sql
     * @param selectionArgs
     * @return
     */
    public List<String> rawQueryForFirstField(String sql,String[] selectionArgs){
    	List<String> returnVal = new LinkedList<String>();
    	SQLiteDatabase db = null;
    	Cursor cursor = null;
    	try{
    		db = getReadableDatabase();
    		cursor = db.rawQuery(sql, selectionArgs);
    		while(true) {
    			if(!cursor.moveToNext()) return returnVal;
				String value = cursor.getString(0);
				if(value == null) value = "";
    			returnVal.add(value);
    		}
    	}finally{
    		try{
    			if(cursor != null) cursor.close();
    		}finally{
    			if(db != null) db.close();
    		}
    	}
    }
    
    /**
     * <p>执行SQL
     * @param sql
     */
    public void execSQL(String sql){
        SQLiteDatabase db = null;
        try {
        	db = getWritableDatabase();
        	db.execSQL(sql);
        }finally{
        	if(db != null) db.close();
        }
    }
    
    /**
     * <p>执行SQL
     * @param sql
     * @param bindArgs
     */
    public void execSQL(String sql,Object[] bindArgs){
        SQLiteDatabase db = null;
        try {
        	db = getWritableDatabase();
        	db.execSQL(sql, bindArgs);
        }finally{
        	if(db != null) db.close();
        }
    }
    
}
