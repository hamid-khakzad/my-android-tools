package cn.emagsoftware.ui.adapterview;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>����adapterview��ص��̳߳أ�adapterview����һЩ�첽����
 * @author Wendell
 * @version 1.1
 */
public final class ThreadPoolManager {
	
	/**�����첽����ִ���̵߳ĸ������ڹ���AsyncDataSchedulerʱ��Ҫ�Դ����maxThreadCount���˴�С�����ж�*/
	public static final int MAX_ASYNCDATA_EXECUTION_THREAD_COUNT = 10;
	/**������������߳������ó��˽���ļ������Ϊ���첽���ݳ���ִ���߳���+�����߳�(AsyncDataScheduler)+�����߳�(BaseLoadingAdapter)*/
	private static final int CORE_POOL_SIZE = 5 + 1 + 1;
	/**�������������߳������ó��˽���ļ������Ϊ���첽�������ִ���߳���+�����߳�(AsyncDataScheduler)+�����߳�(BaseLoadingAdapter)*/
	private static final int MAX_POOL_SIZE = MAX_ASYNCDATA_EXECUTION_THREAD_COUNT + 1 + 1;
	/**���ⴴ�����̱߳�����ĳ�ʱʱ�䣬����Ϊ��λ*/
	private static final int EXTRA_KEEP_ALIVE_TIME = 45;
	/**
	 * ���������ڵ�ǰ�������̳߳ء�����ֱ���ύ����(SynchronousQueue)�����û�п��õ��߳�ʱ�����Եȴ�
	 * ��ʹ���޽����(LinkedBlockingQueue)�ȴ���ԭ���ǣ���δ�ﵽMAX_POOL_SIZEʱ���޽���оͿ�ʼ���Եȴ��ˣ�
	 * �������Ҫ�ﵽ��Ŀ���ǣ���MAX_POOL_SIZE֮�ھ����ȴ����̣߳������ǵȴ��������������ֱ���ύ���ԣ�����д��RejectedExecutionHandler
	 */
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, EXTRA_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true), new RejectedExecutionHandler() {		
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			// TODO Auto-generated method stub
			if(!executor.isShutdown()){
				executor.execute(r);    //����ִ��
			}
		}
	}){
		@Override
		protected void afterExecute(Runnable r,Throwable t){
			super.afterExecute(r,t);
			if(t != null){
				throw new RuntimeException(t);    //������ʽ���׳��쳣�����Ը�ִ���߳���Ҫ���ڲ������쳣�Է�ֹ���׳�
			}
		}
	};
	
	private ThreadPoolManager(){}
	
	/**
	 * <p>ִ���̳߳��е��߳�
	 * @param r
	 */
	public static void executeThread(Runnable r){
		executor.execute(r);
	}
	
	/**
	 * <p>�ر��̳߳أ����е��̶߳������ͷ�
	 */
	public static void shutdownPool(){
		executor.shutdown();
	}
	
}
