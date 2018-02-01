---
###服务端例子
       DoveServer server=new DoveServer("127.0.0.1", 80);
		server.start();
		server.register(TestService.class, new TestServiceImpl());
		DoveServerTest wait=new DoveServerTest();
		synchronized (wait) {
			wait.wait();
		}
###客户端例子
       	DoveClient client = new DoveClient();
		TestService testServiceSyn = client.lookupService("127.0.0.1", 80, TestService.class);
		String result = testServiceSyn.say("hi");
		System.out.println(result);
		TestService testService = client.lookupService("127.0.0.1", 80, TestService.class, msg -> {
			if(msg.isSuccessed()) {
				System.out.println("result:" + msg.getResult());
			}
		});
		
---
---
###系统整体类图
![image](https://github.com/scanry/dove/blob/master/design/%E7%B3%BB%E7%BB%9F%E6%95%B4%E4%BD%93%E7%B1%BB%E5%9B%BE.png)

           