package cn.lihongjie.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于读写锁的计数器
 * @author 982264618@qq.com
 */
public class RRCounter implements Counter {

	private int c;
	private ReentrantReadWriteLock lock;

	public RRCounter(int c) {
		this.c = c;

		lock = new ReentrantReadWriteLock();
	}

	@Override
	public int get() {
		lock.readLock().lock();
		int c = this.c;
		lock.readLock().unlock();
		return c;

	}

	@Override
	public void add(int i) {
		lock.writeLock().lock();

		c = c + i;
		lock.writeLock().unlock();
	}
}
