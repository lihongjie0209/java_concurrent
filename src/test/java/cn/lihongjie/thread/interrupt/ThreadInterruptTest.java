package cn.lihongjie.thread.interrupt;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.nutz.log.Log;

import java.util.concurrent.*;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.nutz.log.Logs.get;

/**
 * @author 982264618@qq.com
 */
public class ThreadInterruptTest {


	private static Log logger = get();

	private static void logInfo(Description description, String status, long nanos) {
		String testName = description.getMethodName();
		logger.info(format("Test %s %s, spent %d microseconds",
				testName, status, NANOSECONDS.toMicros(nanos)));
	}

	@Rule
	public Stopwatch stopwatch = new Stopwatch() {
		@Override
		protected void succeeded(long nanos, Description description) {
			logInfo(description, "succeeded", nanos);
		}

		@Override
		protected void failed(long nanos, Throwable e, Description description) {
			logInfo(description, "failed", nanos);
		}


		@Override
		protected void finished(long nanos, Description description) {
			//			logInfo(description, "finished", nanos);
		}
	};


	/**
	 * 当线程调用阻塞方法时, 必须能够相应中断才能避免一直被阻塞
	 *
	 * @throws Exception
	 */
	@Test
	public void testBlockCallInterrupt() throws Exception {


		Thread thread = new Thread(() -> {


			try {

				logger.info("开始阻塞调用: 休眠1s");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.info("有人要我停止阻塞调用, 那好吧, 我退出");
				logger.info("当前的中断标志位为: " + Thread.currentThread().isInterrupted());
			}

		});


		thread.start();

		thread.interrupt();

		thread.join();


		// 抛出中断异常之后, 中断标志位会被清除
		Assert.assertFalse(thread.isInterrupted());


	}


	/**
	 * 当线程没有阻塞调用时, 也必须有相应的退出机制, 就是在每次开始运行前轮询中断标志位
	 *
	 * @throws Exception
	 */
	@Test
	public void testNonCallInterrupt() throws Exception {


		Thread thread = new Thread(() -> {


			logger.info("线程开始运行");
			while (!Thread.currentThread().isInterrupted()) {
				logger.info(".");

			}

			logger.info("有人通知我退出, 那好吧, 我退出");
			logger.info("当前的中断标志位为: " + Thread.currentThread().isInterrupted());

		});


		thread.start();

		Thread.sleep(10);

		thread.interrupt();

		// 在线程结束之后, 中断标志位返回的都是false
		thread.join();

		logger.info("当前的中断标志位为: " + thread.isInterrupted());
		Assert.assertFalse(thread.isInterrupted());


	}


	/**
	 * 每次抛出中断异常之后中断标志位都会被重置, 如果只捕捉中断异常并没有退出线程, 那么该线程的中断标志就丢失了
	 *
	 * @throws Exception
	 */
	@Test
	public void testLostInterrupt() throws Exception {


		Thread thread = new Thread(() -> {


			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.info("第一次被中断, 当前的中断标志为: " + Thread.currentThread().isInterrupted());
			}
			logger.info("第一次休眠结束, 当前的中断标志为: " + Thread.currentThread().isInterrupted());


			try {
				logger.info("开始第二次休眠");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.info("第二次休眠中断, 当前的中断标志为: " + Thread.currentThread().isInterrupted());
			}


		});


		thread.start();
		thread.interrupt();
		thread.join();
	}

	/**
	 * 捕捉到中断标志位之后如果还想继续运行, 那么就需要重新中断当前线程
	 *
	 * @throws Exception
	 */
	@Test
	public void testFixLostInterrupt() throws Exception {


		Thread thread = new Thread(() -> {


			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.info("第一次被中断, 当前的中断标志为: " + Thread.currentThread().isInterrupted());
				Thread.currentThread().interrupt();
			}
			logger.info("第一次休眠结束, 当前的中断标志为: " + Thread.currentThread().isInterrupted());


			try {
				logger.info("开始第二次休眠");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.info("第二次休眠中断, 当前的中断标志为: " + Thread.currentThread().isInterrupted());
			}


		});


		thread.start();
		thread.interrupt();
		thread.join();
	}


	/**
	 * 定时任务的实现: 有一个线程在规定时间之后中断线程
	 *
	 * @throws Exception
	 */

	private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(2);


	public static void timedRun(Runnable runnable, long timeout, TimeUnit timeUnit) {


		Thread currentThread = Thread.currentThread();
		// 如果任务提前完成, 那么中断怎么处理?
		// 如果执行线程不响应中断, 怎么处理?
		EXECUTOR_SERVICE.schedule(() -> currentThread.interrupt(), timeout, timeUnit);
		runnable.run();

	}

	@Test
	public void testTimedRun() throws Exception {

		ExecutorService threadPool = Executors.newCachedThreadPool();


		Future<?> future = threadPool.submit(() -> {

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.info("被中断了, 开始退出");
				return;
			}


		});

		try {

			future.get(5, TimeUnit.MILLISECONDS);
			logger.info("任务结束");
		} catch (TimeoutException e) {
			logger.info("任务超时");
		} catch (ExecutionException e) {
			logger.info("任务出现了异常");
		}finally {
			future.cancel(true);

		}

	}
}
