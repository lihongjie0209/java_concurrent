package cn.lihongjie.lock;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.nutz.log.Logs.get;

/**
 *
 * 共享可变量在多线程情况下可能会导致程序结果的不正确性
 * @author 982264618@qq.com
 */
public class UnLockedShareVariableTest {

	private static Log logger = get();

	private int shared = 0;
	private final int count = 1000; // 自加次数

	@After
	public void tearDown() throws Exception {
		shared = 0;
	}

	/**
	 * 单线程情况下访问共享可变量没有问题
	 * @throws Exception
	 */
	@Test
	public void singleThreadAdd() throws Exception {

		for (int i = 0; i < count; i++) {
			add();
		}

		Assert.assertThat(shared, Is.is(count));

	}

	private void add() {
		shared = shared + 1;
	}


	/**
	 * 多线程情况下访问共享可变量会出错
	 *
	 * 出错的主要原因是自加操作不具有原子性, 自加操作可以分为三步:
	 * 1. 读取变量 shared
	 * 2. shared 加一
	 * 3. 写入shared到内存
	 *
	 * 原子性操作就意味着该操作要么不发送, 要么发生, 不会处于一个中间状态.当一个线程开始加一的时候, 那么其他线程能观测到的结果只能是
	 * 1. 没有发生: 数据保持不变
	 * 2. 发生了: 数据加一
	 *
	 * 在一个多线程情况下, 如果没有必要的同步, 那么其他线程可能观测到非原子操作的中间状态
	 *
	 * 比如说:
	 * 当线程 1 进行到步骤 2, 线程 2 进行到步骤 1, 那么线程 2 就读取到了线程1 操作的中间状态, 而这种状态是不应该被暴露的
	 *
	 * 为了保证原子性, 我们需要保证在某个具体的时刻, 只能有一个线程访问共享变量
	 *
	 * 但是: 操作系统的线程调度不受程序员的控制
	 *
	 * 我们无法在系统层面控制, 那么我们可以在线程层面控制, 比如说操作系统调度了一个线程, 但是我们不想让这个线程执行, 那么我们可以直接 yield
	 *
	 *
	 *
	 * @throws Exception
	 */
	@Test(expected = AssertionError.class)
	public void multiThreadAdd() throws Exception {

		ExecutorService threadPool = Executors.newCachedThreadPool();
		CountDownLatch countDownLatch = new CountDownLatch(count);
		for (int i = 0; i < count; i++) {
			threadPool.submit(() -> {
				add();
				countDownLatch.countDown();
			});
		}

		countDownLatch.await();
		threadPool.shutdownNow();

		logger.info(String.format("multi thread result is %d", shared));
		Assert.assertThat(shared, Is.is(count));

	}


	@Test
	public void multiThreadAddWithLock() throws Exception {

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

	private synchronized void addSync() {
		add();
	}


}
