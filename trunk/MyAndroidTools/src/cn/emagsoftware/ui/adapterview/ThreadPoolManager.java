package cn.emagsoftware.ui.adapterview;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>用于adapterview相关的线程池，adapterview存在一些异步加载
 * @author Wendell
 * @version 1.2
 */
public final class ThreadPoolManager {
	
	/**池中所保存的线程数*/
	private static final int CORE_POOL_SIZE = 5;
	/**池中允许的最大线程数，当前连接池对线程执行的及时性要求较高，故这里的连接池没有上限*/
	private static final int MAX_POOL_SIZE = Integer.MAX_VALUE;
	/**
	 * 额外创建的线程被清除的超时时间，以秒为单位
	 * 因为可能存在一些频繁的却又快速结束的异步操作，所以这里给出的超时时间较长，从而有效地避免了线程创建和销毁带来的资源消耗，这也是adapterview采用线程池的主要原因之一
	 */
	private static final int EXTRA_KEEP_ALIVE_TIME = 60;
	/**
	 * 创建适用于当前环境的线程池。采用直接提交策略(SynchronousQueue)，因为对线程执行的及时性要求较高
	 * 当前线程池的策略使RejectedExecutionHandler变得无意义，故随便传入一个实现
	 */
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, EXTRA_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy()){
		@Override
		protected void afterExecute(Runnable r,Throwable t){
			super.afterExecute(r,t);
			if(t != null){
				throw new RuntimeException(t);    //这里将显式地抛出异常，所以各执行线程需要在内部处理异常以防止被抛出
			}
		}
	};
	
	private ThreadPoolManager(){}
	
	/**
	 * <p>执行线程池中的线程
	 * @param r
	 */
	public static void executeThread(Runnable r){
		executor.execute(r);
	}
	
	/**
	 * <p>关闭线程池，所有的线程都将被释放
	 */
	public static void shutdownPool(){
		executor.shutdown();
	}
	
}
