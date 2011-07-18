package cn.emagsoftware.ui.adapterview;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>����adapterview��ص��̳߳أ�adapterview����һЩ�첽����
 * @author Wendell
 * @version 1.2
 */
public final class ThreadPoolManager {
	
	/**������������߳���*/
	private static final int CORE_POOL_SIZE = 5;
	/**�������������߳�������ǰ���ӳض��߳�ִ�еļ�ʱ��Ҫ��ϸߣ�����������ӳ�û������*/
	private static final int MAX_POOL_SIZE = Integer.MAX_VALUE;
	/**
	 * ���ⴴ�����̱߳�����ĳ�ʱʱ�䣬����Ϊ��λ
	 * ��Ϊ���ܴ���һЩƵ����ȴ�ֿ��ٽ������첽������������������ĳ�ʱʱ��ϳ����Ӷ���Ч�ر������̴߳��������ٴ�������Դ���ģ���Ҳ��adapterview�����̳߳ص���Ҫԭ��֮һ
	 */
	private static final int EXTRA_KEEP_ALIVE_TIME = 60;
	/**
	 * ���������ڵ�ǰ�������̳߳ء�����ֱ���ύ����(SynchronousQueue)����Ϊ���߳�ִ�еļ�ʱ��Ҫ��ϸ�
	 * ��ǰ�̳߳صĲ���ʹRejectedExecutionHandler��������壬����㴫��һ��ʵ��
	 */
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, EXTRA_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy()){
		@Override
		protected void afterExecute(Runnable r,Throwable t){
			super.afterExecute(r,t);
			if(t != null){
				throw new RuntimeException(t);    //���ｫ��ʽ���׳��쳣�����Ը�ִ���߳���Ҫ���ڲ������쳣�Է�ֹ���׳�
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
