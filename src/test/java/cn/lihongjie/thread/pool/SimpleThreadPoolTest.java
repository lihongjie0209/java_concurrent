package cn.lihongjie.thread.pool;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nutz.log.Log;

import java.util.concurrent.*;

import static org.nutz.log.Logs.get;

/**
 * @author 982264618@qq.com
 */
public class SimpleThreadPoolTest {

	private static Log logger = get();
	private ThreadPoolExecutor executor;


	@Before
	public void setUp() throws Exception {
		/**
		 *
		 *
		 * @param corePoolSize the number of threads to keep in the pool, even
		 *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
		 *        核心数量是线程池在一般情况下的最大值, 除非任务队列满了, 不然线程数量一定小于核心数量
		 * @param maximumPoolSize the maximum number of threads to allow in the
		 *        pool
		 *        线程池的核心线程满了, 任务队列满了, 那么新的任务会创建新线程知道达到最大的线程数
		 * @param keepAliveTime when the number of threads is greater than
		 *        the core, this is the maximum time that excess idle threads
		 *        will wait for new tasks before terminating.
		 *        线程一段时间不活跃之后会被关闭
		 * @param unit the time unit for the {@code keepAliveTime} argument
		 * @param workQueue the queue to use for holding tasks before they are
		 *        executed.  This queue will hold only the {@code Runnable}
		 *        tasks submitted by the {@code execute} method.
		 * @param threadFactory the factory to use when the executor
		 *        creates a new thread
		 * @param handler the handler to use when execution is blocked
		 *        because the thread bounds and queue capacities are reached
		 *        核心线程满, 任务队列满, 最大线程数满, 那么新的任务就会交给handler处理
		 */


		ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(20);
		int corePoolSize = 5;
		executor = new ThreadPoolExecutor(corePoolSize, 10, 1, TimeUnit.SECONDS, blockingQueue);
		executor.allowCoreThreadTimeOut(false);
	}

	@Test
	public void testInitThreadPool() throws Exception {




		// no thread is active util submit task
		Assert.assertThat(executor.getActiveCount(), Is.is(0));

		// submit task will create new thread util reach the core pool size
		executor.submit(() -> logger.info(executor));
		Assert.assertThat(executor.getActiveCount(), Is.is(1));


		executor.shutdown();
	}


	/**
	 * 无法提交任务到已经关闭的线程池, 会抛出异常
	 * @throws Exception
	 */
	@Test(expected = java.util.concurrent.RejectedExecutionException.class)
	public void submitTaskToShutDownThreadPool() throws Exception {


		executor.shutdown();

		executor.submit(() -> {
		});


	}

	/**
	 * shutdown thread pool will reject new task but thread in pool will not affected
	 * @throws Exception
	 */
	@Test
	public void shutDownThreadPoolWhileThreadIsActive() throws Exception {


		CountDownLatch latch = new CountDownLatch(1);


		executor.submit(() -> {

			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			latch.countDown();

			logger.info("I am running");
		});

		logger.info("start shutdown thread pool");
		executor.shutdown();
		logger.info("shutdown thread pool complete");
		latch.await();


	}

	/**
	 * shutdown now thread pool will Interrupte all the thread in the pool
	 *
	 * @throws Exception
	 */
	@Test
	public void shutDownNowThreadPoolWhileThreadIsActive() throws Exception {


		CountDownLatch latch = new CountDownLatch(1);


		executor.submit(() -> {

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				logger.error(e);
				return;
			}
			latch.countDown();

			logger.info("I am running");
		});

		logger.info("start shutdown thread pool");
		executor.shutdownNow();
		logger.info("shutdown thread pool complete");
		latch.await(2, TimeUnit.SECONDS);


	}


	/**
	 * use shutdown to stop incoming request
	 *
	 * use terminate to wait all the thread to complete
	 *
	 * @throws Exception
	 */
	@Test
	public void terminateThreadPoolWhileThreadIsActive() throws Exception {


		CountDownLatch latch = new CountDownLatch(1);


		executor.submit(() -> {

			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				logger.error(e);
				return;
			}
			latch.countDown();

			logger.info("I am running");
		});

		logger.info("start Termination thread pool");
		executor.shutdown();
		executor.awaitTermination(100, TimeUnit.SECONDS);
		logger.info("Termination thread pool complete");
		latch.await();


	}


	@Test(expected = RejectedExecutionException.class)
	public void testThreadPoolRejection() throws Exception {


		runWithRejectionPolicy(new ThreadPoolExecutor.AbortPolicy());

	}

	@Test
	public void testThreadPoolRejectionCallerRun() throws Exception {


		runWithRejectionPolicy(new ThreadPoolExecutor.CallerRunsPolicy());

	}
	private void runWithRejectionPolicy(RejectedExecutionHandler rejectedExecutionHandler) throws InterruptedException {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(
				1,
				1,
				0,
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(1));


		executor.setRejectedExecutionHandler(rejectedExecutionHandler); // default
		// 第一个任务交给核心线程
		executor.submit(new Runnable() {
			@Override
			public void run() {


				logger.info("first task run");
			}
		});


		// 第二个任务放在等待队列中
		executor.submit(new Runnable() {
			@Override
			public void run() {


				logger.info("second task run");
			}
		});

		// 第三个任务被抛弃
		executor.submit(new Runnable() {
			@Override
			public void run() {


				logger.info("third task run");
			}
		});


		executor.awaitTermination(1, TimeUnit.SECONDS);
	}
}
