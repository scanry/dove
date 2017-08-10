package six.com.rpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import six.com.rpc.client.NettyRpcCilent;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月10日 下午2:44:35
 */
public class RpcClientTest{

	static volatile long allTime = 0;
	static volatile int index = 0;

	public static void main(String[] args) {
		NettyRpcCilent client = new NettyRpcCilent();
		String targetHost = "127.0.0.1";
		int targetPort = 8180;
		int requestCount =500;
		CountDownLatch cdl = new CountDownLatch(requestCount);
		String name = "six";
		ExecutorService executor = Executors.newFixedThreadPool(20);
		for (int i = 0; i < requestCount; i++) {
			TestService testService = client.lookupService(targetHost, targetPort, TestService.class, result -> {
				System.out.println("result:" + result);
				cdl.countDown();
			});
//			TestService testService = client.lookupService(targetHost, targetPort, TestService.class);
//			try {
//				long startTime = System.currentTimeMillis();
//				Object result = testService.say(name + "-" + index++);
//				System.out.println(result);
//				long endTime = System.currentTimeMillis();
//				long totalTime = endTime - startTime;
//				allTime += totalTime;
//				System.out.println("消耗时间:" + totalTime);
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				cdl.countDown();
//			}
			executor.execute(() -> {
				try {
					long startTime = System.currentTimeMillis();
					Object result = testService.say(name + "-" + index++);
					System.out.println(result);
					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					allTime += totalTime;
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
		System.out.println("总消耗时间:" + allTime);
		System.out.println("平均消耗时间:" + allTime / index);
		client.destroy();
		executor.shutdown();
	}
}
