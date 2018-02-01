package six.com.rpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.six.dove.rpc.client.DoveClient;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月10日 下午2:44:35
 */
public class DoveClientTest {

	static AtomicLong allTimeSyn = new AtomicLong(0);
	static AtomicLong allTime = new AtomicLong(0);

	public static void main(String[] args) {
		DoveClient client = new DoveClient();
		String targetHost = "127.0.0.1";
		int targetPort = 80;
		int requestCount = 500;
		CountDownLatch cdl = new CountDownLatch(requestCount);
		String name = "six";
		ExecutorService executor = Executors.newFixedThreadPool(20);
		TestService testServiceSyn = client.lookupService(targetHost, targetPort, TestService.class);
		TestService testService = client.lookupService(targetHost, targetPort, TestService.class, result -> {
			System.out.println("result:" + result.getResult());
			cdl.countDown();
		});
		for (int i = 0; i < requestCount; i++) {
			try {
				long startTime = System.currentTimeMillis();
				Object result = testServiceSyn.say(name);
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
					Object result = testService.say(name);
					System.out.println(result);
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
