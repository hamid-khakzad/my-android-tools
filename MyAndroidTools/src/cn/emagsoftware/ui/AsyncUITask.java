package cn.emagsoftware.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;
import android.view.View;

/**
 * <p>ִ�и���UI���첽���ݲ���ǿ���Ƽ�ʹ�ô��࣬�����ڲ���UIʹ���������ã�������UI������
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
			if(views[i] == null) return;    //ֻҪ��һ��view�����գ��ͽ����ٻص�onPostExecute�����⴫��ʹ�����View������һ�´�����һϵ������
		}
		onPostExecute(views,result);
	};
	
}
