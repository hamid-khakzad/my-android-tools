package cn.emagsoftware.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;

/**
 * <p>����������ִ���������ض�����(����Ϊ������)���첽�������������ݱ����պ������ȡ��������������ڲ�����������ʹ���������ã��������䱻����
 * @author Wendell
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class AsyncWeakTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	
	private List<WeakReference<Object>> mObjReferences = null;
	
	public AsyncWeakTask(Object... objs){
		mObjReferences = new ArrayList<WeakReference<Object>>(objs.length);
		for(Object obj:objs){
			if(obj == null) throw new NullPointerException();
			mObjReferences.add(new WeakReference<Object>(obj));
		}
	}
	
	protected abstract void onPostExecute(Object[] objs,Result result);
	
	@Override
	protected final void onPostExecute(Result result) {
		Object[] objs = new Object[mObjReferences.size()];
		Iterator<WeakReference<Object>> objIterator = mObjReferences.iterator();
		for(int i = 0;i < objs.length;i++){
			objs[i] = objIterator.next().get();
			if(objs[i] == null) return;    //ֻҪ��һ��Object�����գ���ȡ����ǰ������Ϊ����ʹ�����Object������һ�¿��ܴ���һϵ������
		}
		onPostExecute(objs,result);
	};
	
}
