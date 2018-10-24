package cn.lihongjie.nolock;

import org.hamcrest.core.Is;
import org.junit.*;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.nutz.log.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.nutz.log.Logs.get;

/**
 * 线程同步只有两种方法:
 *
 * 1. 轮询
 * 2. 上锁
 *
 *
 * 如果一个对象被称为无锁, 那么它一定是轮询的
 *
 *
 * 相对于轮询, 上锁时进行的上下文切换的开销是巨大的, 当马上能获得锁时进行上下文切换是不理智的
 *
 * 所以对于一些操作非常简单的对象, 使用轮询能获得更好的性能
 *
 *
 * @author 982264618@qq.com
 */
public class NoLockTest {


	private static int counter = 0;
	private static AtomicLong atomicLong = new AtomicLong(0);


	private static Log logger = get();

	private ExecutorService threadPool;
	private int threadCount = 10;
	private int sumPerThread = 1000;


	private static void logInfo(Description description, String status, long nanos) {
		String testName = description.getMethodName();
		logger.info(String.format("Test %s %s, spent %d microseconds",
				testName, status, TimeUnit.NANOSECONDS.toMicros(nanos)));
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


	@Before
	public void setUp() throws Exception {
		threadPool = Executors.newCachedThreadPool();
	}

	@After
	public void tearDown() throws Exception {
		threadPool.shutdownNow();
	}


	@Test
	public void testSyncAdd() throws Exception {

		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {


			threadPool.submit(() -> {


				for (int j = 0; j < sumPerThread; j++) {
					add();
				}
			latch.countDown();

			});
		}

		latch.await();
		Assert.assertThat(counter, Is.is(threadCount * sumPerThread));



	}


	@Test
	public void testAtomicAdd() throws Exception {
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {


			threadPool.submit(() -> {


				for (int j = 0; j < sumPerThread; j++) {
					atomicLong.addAndGet(1);
				}
			latch.countDown();

			});

		}

		latch.await();

		int value = threadCount * sumPerThread;
		Assert.assertThat(((long) (atomicLong.get())), Is.is(((long) (value))));


	}



	private synchronized static void add() {

		counter = counter + 1;

	}
}
