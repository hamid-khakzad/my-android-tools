package cn.emagsoftware.ui.adapterview;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>用于adapterview相关的线程池，adapterview存在一些异步加载
 * @author Wendell
 * @version 1.1
 */
public final class ThreadPoolManager {
	
	/**最大的异步数据执行线程的个数，在构建AsyncDataScheduler时需要对传入的maxThreadCount作此大小限制判断*/
	public static final int MAX_ASYNCDATA_EXECUTION_THREAD_COUNT = 10;
	/**池中所保存的线程数，得出此结果的计算策略为：异步数据常用执行线程数+调度线程(AsyncDataScheduler)+加载线程(BaseLoadingAdapter)*/
	private static final int CORE_POOL_SIZE = 5 + 1 + 1;
	/**池中允许的最大线程数，得出此结果的计算策略为：异步数据最大执行线程数+调度线程(AsyncDataScheduler)+加载线程(BaseLoadingAdapter)*/
	private static final int MAX_POOL_SIZE = MAX_ASYNCDATA_EXECUTION_THREAD_COUNT + 1 + 1;
	/**额外创建的线程被清除的超时时间，以秒为单位*/
	private static final int EXTRA_KEEP_ALIVE_TIME = 45;
	/**
	 * 创建适用于当前环境的线程池。采用直接提交策略(SynchronousQueue)，如果没有可用的线程时将尝试等待
	 * 不使用无界队列(LinkedBlockingQueue)等待的原因是：当未达到MAX_POOL_SIZE时，无界队列就开始尝试等待了，
	 * 而这边想要达到的目的是：在MAX_POOL_SIZE之内均优先创建线程，而不是等待，所以这里采用直接提交策略，并重写了RejectedExecutionHandler
	 */
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, EXTRA_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true), new RejectedExecutionHandler() {		
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			// TODO Auto-generated method stub
			if(!executor.isShutdown()){
				executor.execute(r);    //重新执行
			}
		}
	}){
		@Override
		protected void afterExecute(Runnable r,Throwable t){
			super.afterExecute(r,t);
			if(t != null){
				throw new RuntimeException(t);    //这里显式地抛出异常，所以各执行线程需要在内部处理异常以防止被抛出
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
