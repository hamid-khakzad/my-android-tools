package cn.emagsoftware.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;
import android.view.View;

/**
 * <p>执行更新UI的异步数据操作强烈推荐使用此类，该类内部对UI使用了虚引用，不妨碍UI被回收
 * @author Wendell
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class AsyncUITask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	
	private List<WeakReference<View>> mViewReferences = null;
	
	public AsyncUITask(View... views){
		mViewReferences = new ArrayList<WeakReference<View>>(views.length);
		for(View view:views){
			if(view == null) throw new NullPointerException();
			mViewReferences.add(new WeakReference<View>(view));
		}
	}
	
	protected abstract void onPostExecute(View[] views,Result result);
	
	@Override
	protected final void onPostExecute(Result result) {
		View[] views = new View[mViewReferences.size()];
		Iterator<WeakReference<View>> viewIterator = mViewReferences.iterator();
		for(int i = 0;i < views.length;i++){
			views[i] = viewIterator.next().get();
			if(views[i] == null) return;    //只要有一个view被回收，就将不再回调onPostExecute，避免传入和传出的View个数不一致带来的一系列问题
		}
		onPostExecute(views,result);
	};
	
}
