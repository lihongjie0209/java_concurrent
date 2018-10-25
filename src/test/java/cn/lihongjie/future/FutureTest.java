package cn.lihongjie.future;

import org.hamcrest.core.Is;
import org.junit.*;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.nutz.log.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.nutz.log.Logs.get;

/**
 * @author 982264618@qq.com
 */
public class FutureTest {


	private static Log logger = get();
	private ExecutorService threadPool;

	private static void logInfo(Description description, String status, long nanos) {
		String testName = description.getMethodName();
		logger.info(format("Test %s %s, spent %d Milliseconds",
				testName, status, NANOSECONDS.toMillis(nanos)));
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
		threadPool.shutdown();

	}

	@Test(expected = TimeoutException.class)
	public void testFutureTimeOut() throws Exception {


		Future<?> future = threadPool.submit(() -> {


			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});



		future.get(500, TimeUnit.MILLISECONDS);

	}


	/**
	 *
	 * 批量任务如果超时会被直接取消
	 *
	 * @throws Exception
	 */
	@Test(expected = CancellationException.class)
	public void testBatchTask() throws Exception {


		Callable<Void> task2 = () -> {


			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;

		};
		Callable<Void> task1 = () -> {


			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return null;
		};


		List<Future<Void>> futures = threadPool.invokeAll(Arrays.<Callable<Void>>asList(task1, task2), 200, TimeUnit.MILLISECONDS);


		// 被取消的任务会有标记
		Assert.assertThat(futures.stream().filter(item->item.isCancelled()).count(), Is.is(1L));


		// 所有任务都会完成, 如果超时就中断
		Assert.assertThat(futures.stream().filter(item->item.isDone()).count(), Is.is(2L));

		futures.stream().filter(item -> item.isCancelled()).findAny().ifPresent(item -> {
			try {
				item.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});
	}








}
