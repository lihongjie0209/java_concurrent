package cn.lihongjie.lock;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.nutz.log.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.nutz.log.Logs.get;

/**
 * @author 982264618@qq.com
 */
public class LockedShareVariableTest {

	private static Log logger = get();

	private int shared = 0;
	private final int count = 1000; // 自加次数

	private Lock lock = new ReentrantLock();  // lock 是一个变量, 使用之前声明

	@After
	public void tearDown() throws Exception {
		shared = 0;
	}


	private void add() {
		shared = shared + 1;
	}


	@Test
	public void multiThreadAddWithSync() throws Exception {

		ExecutorService threadPool = Executors.newCachedThreadPool();
		CountDownLatch countDownLatch = new CountDownLatch(count);
		for (int i = 0; i < count; i++) {
			threadPool.submit(() -> {
				addSync();
				countDownLatch.countDown();
			});
		}

		countDownLatch.await();
		threadPool.shutdownNow();

		logger.info(String.format("multi thread result is %d", shared));
		Assert.assertThat(shared, Is.is(count));

	}


	/**
	 * 锁只给我们提供了一个最简单的同步机制, 即: 在某一个具体时刻, 只能有一个线程在运行
	 * @throws Exception
	 */
	@Test
	public void multiThreadAddWithLock() throws Exception {

		ExecutorService threadPool = Executors.newCachedThreadPool();
		CountDownLatch countDownLatch = new CountDownLatch(count);
		for (int i = 0; i < count; i++) {
			threadPool.submit(() -> {
				addWithLock();
				countDownLatch.countDown();
			});
		}

		countDownLatch.await();
		threadPool.shutdownNow();

		logger.info(String.format("multi thread result is %d", shared));
		Assert.assertThat(shared, Is.is(count));

	}

	private void addWithLock() {

		lock.lock();
		try {

			add();
		} finally {

			lock.unlock();
		}


	}


	private synchronized void addSync() {
		add();
	}


}
