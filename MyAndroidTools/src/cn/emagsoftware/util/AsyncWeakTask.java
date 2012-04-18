package cn.emagsoftware.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;

/**
 * <p>此类适用于执行依赖于特定数据(尤其为大数据)的异步操作且依赖数据被回收后任务可取消的情况，该类内部对依赖数据使用了虚引用，不妨碍其被回收
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
			if(objs[i] == null) return;    //只要有一个Object被回收，就取消当前任务，因为传入和传出的Object个数不一致可能带来一系列问题
		}
		onPostExecute(objs,result);
	};
	
}
