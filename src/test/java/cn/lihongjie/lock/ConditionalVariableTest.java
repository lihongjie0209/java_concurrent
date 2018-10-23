package cn.lihongjie.lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nutz.log.Log;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.nutz.log.Logs.get;

/**
 * 条件变量是线程同步的重要工具
 * <p>
 * <p>
 * 条件变量是一个共享的可变量, 访问必须进行同步(上锁)
 * <p>
 * 对条件变量的操作可以有:
 * <p>
 * wait 等待其他人修改共享变量状态, 同时释放锁
 * notify 通知其他人状态已经被修改, 同时获取锁
 *
 * @author 982264618@qq.com
 */
public class ConditionalVariableTest {


	private static Log logger = get();

	private ExecutorService threadPool;

	@Before
	public void setUp() throws Exception {
		threadPool = Executors.newCachedThreadPool();
	}

	@After
	public void tearDown() throws Exception {
		threadPool.shutdownNow();
	}

	/**
	 * 使用条件变量如果single在wait之前, 那么信号就会丢失
	 * <p>
	 * 所以除了条件变量作为一个事件, 我们还需要一个共享变量来轮询
	 *
	 *
	 * A condition variable is generally used to avoid busy waiting (looping repeatedly while checking a condition) while waiting for a resource to become available. For instance, if you have a thread (or multiple threads) that can't continue onward until a queue is empty, the busy waiting approach would be to just doing something like:

	 //pseudocode
	 while(!queue.empty())
	 {
	 sleep(1);
	 }
	 The problem with this is that you're wasting processor time by having this thread repeatedly check the condition. Why not instead have a synchronization variable that can be signaled to tell the thread that the resource is available?

	 //pseudocode
	 syncVar.lock.acquire();

	 while(!queue.empty())
	 {
	 syncVar.wait();
	 }

	 //do stuff with queue

	 syncVar.lock.release();
	 * @throws Exception
	 */
	@Test(expected = Exception.class)

	public void testConditionVarWithoutStateVariable() throws Exception {


		ReentrantLock lock = new ReentrantLock();

		Condition condition = lock.newCondition();

		CyclicBarrier barrier = new CyclicBarrier(3);


		threadPool.submit(() -> {
			try {

				lock.lock();
				logger.info("准备发送信号");
				condition.signal();
				logger.info("发送信号成功");

			}  finally {

				lock.unlock();
			}
			try {
				barrier.await(); // 表示线程已经完成了工作
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}

		});
		threadPool.submit(() -> {
			try {

				lock.lock();
				logger.info("准备接收信号");
				condition.await();
				logger.info("接收信号成功");

			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {

				lock.unlock();
			}

			try {
				barrier.await(); // 表示线程已经完成了工作
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		});


		barrier.await(5, TimeUnit.SECONDS);


	}


	@Test
	public void testConditionVarWithStateVariable() throws Exception {


		final boolean[] done = {false};
		ReentrantLock lock = new ReentrantLock();

		Condition condition = lock.newCondition();

		CyclicBarrier barrier = new CyclicBarrier(3);


		threadPool.submit(() -> {
			try {

				Thread.sleep(1000);
				lock.lock();
				logger.info("准备发送信号");
				done[0] = true;
				condition.signal();
				logger.info("发送信号成功");

			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {

				lock.unlock();
			}
			try {
				barrier.await(); // 表示线程已经完成了工作
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}

		});
		threadPool.submit(() -> {
			try {

				lock.lock();
				logger.info("准备接收信号");
				while (done[0] == false) {
					logger.info("开始轮询");
					condition.await();
					logger.info("有人通知我了");
				}
				logger.info("接收信号成功");

			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {

				lock.unlock();
			}
			try {
				barrier.await(); // 表示线程已经完成了工作
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}

		});


		barrier.await(5, TimeUnit.SECONDS);


	}

	@Test
	public void testConditionVarUseSemaphore() throws Exception {


		Semaphore semaphore = new Semaphore(0);


		CyclicBarrier barrier = new CyclicBarrier(3);


		threadPool.submit(() -> {
			try {

				Thread.sleep(1000);
				logger.info("准备发送信号");

				logger.info("发送信号成功");

			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {

				semaphore.release();
			}
			try {
				barrier.await(); // 表示线程已经完成了工作
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}

		});
		threadPool.submit(() -> {
			try {

				logger.info("准备接收信号");
				semaphore.acquire();
				logger.info("接收信号成功");

			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {

			}
			try {
				barrier.await(); // 表示线程已经完成了工作
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}

		});


		barrier.await(5, TimeUnit.SECONDS);


	}

}
