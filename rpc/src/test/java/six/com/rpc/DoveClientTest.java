package six.com.rpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.six.dove.rpc.client.DoveClientImpl;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月10日 下午2:44:35
 */
public class DoveClientTest {

	static AtomicLong allTimeSyn = new AtomicLong(0);
	static AtomicLong allTime = new AtomicLong(0);

	public static void main(String[] args) {
		DoveClientImpl client = new DoveClientImpl();
		TestService testServiceSyn = client.lookupService("127.0.0.1", 80, TestService.class);
		String result = testServiceSyn.say("hi");
		System.out.println(result);
		TestService testService = client.lookupService("127.0.0.1", 80, TestService.class, msg -> {
			if(msg.isSuccessed()) {
				System.out.println("result:" + msg.getResult());
			}
		});
		int requestCount = 500;
		CountDownLatch cdl = new CountDownLatch(requestCount);
		String name = "six";
		ExecutorService executor = Executors.newFixedThreadPool(20);
		for (int i = 0; i < requestCount; i++) {
			try {
				long startTime = System.currentTimeMillis();
				result = testServiceSyn.say(name);
				System.out.println(result);
				long endTime = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				allTimeSyn.set(allTimeSyn.get() + totalTime);
				System.out.println("消耗时间:" + totalTime);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// cdl.countDown();
			}
			executor.execute(() -> {
				try {
					long startTime = System.currentTimeMillis();
					System.out.println(testService.say(name));
					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					allTime.set(allTime.get() + totalTime);
					System.out.println("消耗时间:" + totalTime);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("同步总消耗时间:" + allTimeSyn);
		System.out.println("同步平均消耗时间:" + allTimeSyn.get() / requestCount);
		System.out.println("异步总消耗时间:" + allTime);
		System.out.println("异步平均消耗时间:" + allTime.get() / requestCount);
		client.stop();
		executor.shutdown();
	}
}
