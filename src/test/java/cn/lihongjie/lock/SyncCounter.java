package cn.lihongjie.lock;

/**
 * @author 982264618@qq.com
 */
public class SyncCounter implements Counter {


	private int c;

	public SyncCounter(int c) {
		this.c = c;
	}

	@Override
	public synchronized int get() {
		return c;
	}


	@Override
	public synchronized void add(int i) {

		c = c + i;
	}




}
