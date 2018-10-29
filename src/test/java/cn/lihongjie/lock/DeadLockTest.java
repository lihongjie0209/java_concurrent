package cn.lihongjie.lock;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.nutz.log.Log;

import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.nutz.log.Logs.get;

/**
 * @author 982264618@qq.com
 */
public class DeadLockTest {

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
	 *
	 * 死锁存在的条件
	 * 1. 有多个锁
	 * 2. 多个线程以不同的顺序获取锁\
	 *
	 * 解决方案:
	 * 1. 锁的获取必须保证全局的顺序
	 * 2. 定时锁, 如果在一定时间内没有获得锁, 那么就放弃
	 * @throws Exception
	 */
	@Test
	@Ignore("死锁, 不要运行")
	public void testDeadLock() throws Exception {

		ReentrantLock lockA = new ReentrantLock();
		ReentrantLock lockB = new ReentrantLock();

		// 线程A获取A锁, 然后获取B锁

		Thread ta = new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lockA.lock();
				logger.info("获取A锁成功");
				logger.info("开始获取B锁");
				lockB.lock();
				logger.info("获取B锁成功");
				lockB.unlock();
				lockA.unlock();
			}
		});


		Thread tb = new Thread(new Runnable() {
			@Override
			public void run() {

				logger.info("开始获取B锁");
				lockB.lock();
				logger.info("获取B锁成功");
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				logger.info("开始获取A锁");
				lockA.lock();
				logger.info("获取A锁成功");
				lockA.unlock();
				lockB.unlock();
			}
		});

		ta.start();
		tb.start();
		ta.join();
		tb.join();


	}
}
