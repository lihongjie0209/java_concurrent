package cn.lihongjie.lock;

import org.hamcrest.core.Is;
import org.junit.*;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.nutz.log.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.nutz.log.Logs.get;

/**
 * 读写锁只有在读写比例在一定的情况下才能比一般的锁的性能好
 *
 * @author 982264618@qq.com
 */
public class ReadWriteLockTest {
	private static Log logger = get();

	private static final int count = 10000;
	private ExecutorService threadPool;


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
	public void testWriteWithSyncCounter() throws Exception {

		SyncCounter counter = new SyncCounter(0);


		singleThreadAdd(counter);


	}


	@Test
	public void testWriteWithSyncCounterConcurrent() throws Exception {

		SyncCounter counter = new SyncCounter(0);


		concurrentThreadAdd(counter, 10);


	}


	@Test
	public void testReadWriteWithSyncCounterConcurrent() throws Exception {

		SyncCounter counter = new SyncCounter(0);


		concurrentThreadAddAndGet(counter, 10);


	}

	private void concurrentThreadAddAndGet(Counter counter, int i) throws InterruptedException {
		concurrentThreadAdd(counter, i);
		concurrentThreadGet(counter, i);
	}

	private void concurrentThreadGet(Counter counter, int threadCount) throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(threadCount);
		int each = count / threadCount;
		for (int i = 0; i < threadCount; i++) {


			int finalI = i;
			threadPool.submit(() -> {


				for (int j = finalI * each; j < (finalI + 1) * each; j++) {

					counter.get();


				}

				latch.countDown();

			});


		}


		latch.await();

		Assert.assertThat(counter.get(), Is.is(count));
	}

	private void concurrentThreadAdd(Counter counter, int threadCount) throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(threadCount);
		int each = count / threadCount;
		for (int i = 0; i < threadCount; i++) {


			int finalI = i;
			threadPool.submit(() -> {


				for (int j = finalI * each; j < (finalI + 1)* each ; j++) {

					counter.add(1);


				}

				latch.countDown();

			});




		}



		latch.await();

		Assert.assertThat(counter.get(), Is.is(count));


	}


	@Test
	public void testWriteWithRRCounter() throws Exception {

		Counter counter = new RRCounter(0);


		singleThreadAdd(counter);


	}

	@Test
	public void testWriteWithRRCounterConcurrent() throws Exception {

		Counter counter = new RRCounter(0);


		concurrentThreadAdd(counter, 10);


	}


	@Test
	public void testReadWriteWithRRCounterConcurrent() throws Exception {

		Counter counter = new RRCounter(0);


		concurrentThreadAddAndGet(counter, 10);


	}
	private void singleThreadAdd(Counter counter) {
		for (int i = 0; i < count; i++) {
			counter.add(1);
		}


		Assert.assertThat(counter.get(), Is.is(count));
	}
}
