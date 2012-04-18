package cn.emagsoftware.util;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;

public class GenericApplication extends Application {
	
	private static GenericApplication APP = null;
	
	private Map<String, Object> mMap = new HashMap<String, Object>();
	
	public static GenericApplication getInstance(){
		return APP;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		APP = this;
	}
	
	public void setValue(String key,Object value){
		mMap.put(key, value);
	}
	
	public Object getValue(String key){
		return mMap.get(key);
	}
	
}
