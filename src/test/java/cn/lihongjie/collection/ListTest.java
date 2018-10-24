package cn.lihongjie.collection;

import org.hamcrest.core.Is;
import org.junit.*;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.rules.Stopwatch;
import org.nutz.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static org.nutz.log.Logs.get;

/**
 * @author 982264618@qq.com
 */
public class ListTest {

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
	public void init() throws Exception {

	}

	@Test
	public void testArrayListAppend() throws Exception {


		ArrayList<Integer> list = new ArrayList<>();


		singleThreadAppend(list);


	}

	private void singleThreadAppend(List<Integer> list) {
		for (int i = 0; i < count; i++) {

			list.add(i);
		}

		Assert.assertThat(list.size(), Is.is(count));
	}


	@Test
	public void testSyncArrayListAppend() throws Exception {


		List<Integer> list = Collections.synchronizedList(new ArrayList<>());


	singleThreadAppend(list);


	}


	/**
	 * linkedlist 不需要resize,  添加和插入性能会好一点
	 * @throws Exception
	 */
	@Test
	public void testLinkedListAppend() throws Exception {


		List<Integer> list = new LinkedList<>();

singleThreadAppend(list);


	}


	@Test(expected = AssertionError.class)
	public void testArrayListConcurrentAppend() throws Exception {


		ArrayList<Integer> list = new ArrayList<>();

		concurrentAppend(list);


	}

	private void concurrentAppend(List<Integer> list) throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(2);
		threadPool.submit(()->{

		for (int i = 0; i < count/2; i++) {

			list.add(i);
		}

		latch.countDown();
		});

		threadPool.submit(() -> {

			for (int i = count / 2; i <count; i++) {

				list.add(i);
			}

			latch.countDown();
		});

		latch.await();


		Assert.assertThat(list.size(), Is.is(count));
	}


	@Test(expected = AssertionError.class)
	public void testLinkedListConcurrentAppend() throws Exception {


		List<Integer> list = new LinkedList<>();

		concurrentAppend(list);


	}

	@Test
	public void testSyncArrayListConcurrentAppend() throws Exception {


		List<Integer> list = Collections.synchronizedList(new ArrayList<>());

		concurrentAppend(list);


	}

	/**
	 * 线程安全的数据结构会继续线程同步, 而同步导致的线程切换在一些场景下会带来性能问题
	 * @throws Exception
	 */
	@Test
	public void testSyncLinkedListConcurrentAppend() throws Exception {


		List<Integer> list = Collections.synchronizedList(new LinkedList<>());

		concurrentAppend(list);


	}


	/**
	 *
	 * 只写不读的情况下, 性能太差了
	 *
	 * @throws Exception
	 */
	@Test
	public void testCopyOnWriteArrayListConcurrentAppend() throws Exception {


		List<Integer> list = new CopyOnWriteArrayList<>();

		concurrentAppend(list);


	}


}
