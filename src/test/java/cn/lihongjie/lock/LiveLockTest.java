package cn.lihongjie.lock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.nutz.log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.nutz.log.Logs.get;

/**
 * @author 982264618@qq.com
 */
public class LiveLockTest {

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
	 * 活锁是指线程重复执行相同的动作而无法继续
	 *
	 * 死锁的和活锁都会导致线程进行运行
	 * 但是死锁是在等待
	 * 活锁是在运行, 一般出现在重试中, 如事务提交
	 * @throws Exception
	 */
	@Test
	public void testLiveLock() throws Exception {


		ConcurrentLinkedQueue<Integer> jobQueue = new ConcurrentLinkedQueue<>();

		jobQueue.addAll(Arrays.asList(1, 2, 3, 4));


		while (!jobQueue.isEmpty()) {

			Integer head = jobQueue.peek();


			try {

				handleJob(head);
				jobQueue.remove();
			} catch (Exception e) {

				logger.info("回滚任务" + String.valueOf(head));
			}



		}


	}

	private void handleJob(Integer head) {

		logger.info("开始处理任务 " + String.valueOf(head));
		if (head == 2) {
			throw new RuntimeException();
		}
		logger.info("处理任务 " + String.valueOf(head) + " 完成");
	}
}
