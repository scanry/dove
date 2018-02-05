---系统功能列表
1.	远程调用模块(基于netty实现完成)
2.	rpc客户端基本功能(完成)
3.	rpc客户端兼容spring(待完成)
4.	rpc服务端基本功能(完成)
5.	rpc服务端兼容spring(待完成)
6.	rpc服务注册器(待完成)
7.	服务负载均衡(待完成)
8.	服务调用监控(待完成)
9.	服务熔断(待完成)
10.	其他功能以后待加入
---
###服务端例子
```
DoveServer server=new DoveServer("127.0.0.1", 80);
server.start();
server.register(TestService.class, new TestServiceImpl());
DoveServerTest wait=new DoveServerTest();
synchronized (wait) {
	wait.wait();
}
```
###客户端例子
```
DoveClient client = new DoveClient();
TestService testServiceSyn = client.lookupService("127.0.0.1", 80, TestService.class);
String result = testServiceSyn.say("hi");
System.out.println(result);
TestService testService = client.lookupService("127.0.0.1", 80, TestService.class, msg -> {
	if(msg.isSuccessed()) {
		System.out.println("result:" + msg.getResult());
	}
});
```
---
###系统用户场景图
![image](https://github.com/scanry/dove/blob/master/design/%E7%B3%BB%E7%BB%9F%E7%94%A8%E6%88%B7%E5%9C%BA%E6%99%AF%E5%9B%BE.png)
---
###系统整体类图
![image](https://github.com/scanry/dove/blob/master/design/%E7%B3%BB%E7%BB%9F%E6%95%B4%E4%BD%93%E7%B1%BB%E5%9B%BE.png)

           