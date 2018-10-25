package cn.lihongjie.future;

import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.builders.JUnit3Builder;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.nutz.log.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.*;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.nutz.log.Logs.get;

/**
 * @author 982264618@qq.com
 */
public class CompletionServiceTest {

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
	 * 默认的ExecutorService会给每一个任务都返回一个future, 我们如果想知道结果, 必须对每一个future进行轮询, 效率低
	 * @throws Exception
	 */
	@Test
	public void beforeCompletionService() throws Exception {


		ExecutorService threadPool = Executors.newCachedThreadPool();


		LinkedList<Future<Integer>> futures = new LinkedList<>();
		for (int i = 0; i < 5; i++) {

			Future<Integer> future = threadPool.submit(() -> {

				Thread.sleep(1000);

				return 1;
			});

			futures.add(future);
		}


		for (Future<Integer> future : futures) {

			// 内部的实现是轮询, 效率低
			future.get();
		}



		threadPool.shutdown();


	}


	/**
	 * ExecutorCompletionService 在内部封装了一个结果queue, 我们只需要poll这个queue就可以避免轮询future了
	 *
	 * 实现原理是在task中添加一个回调函数, 当task结束之后通过这个回调函数把结果放入结果queue
	 *
	 * @throws Exception
	 */
	@Test
	public void withCompletionService() throws Exception {

		ExecutorService threadPool = Executors.newCachedThreadPool();
		ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(threadPool);


		for (int i = 0; i < 5; i++) {

			Future<Integer> future = completionService.submit(() -> {

				Thread.sleep(1000);

				return 1;
			});

		}


		for (int i = 0; i < 5; i++) {

			completionService.poll();
		}

		threadPool.shutdown();
	}
}
