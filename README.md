---
###目录
---
1.	系统图
2.	系统功能列表
3.	系统使用例子
---
###系统图
---
一. 系统结构图
![image](https://github.com/scanry/dove/blob/master/design/%E4%BD%93%E7%B3%BB%E7%BB%93%E6%9E%84%E5%9B%BE.png)
---
二. 系统用户场景图
![image](https://github.com/scanry/dove/blob/master/design/%E7%B3%BB%E7%BB%9F%E7%94%A8%E6%88%B7%E5%9C%BA%E6%99%AF%E5%9B%BE.png)
---
三. 客户端调用流程图
![image](https://github.com/scanry/dove/blob/master/design/%E5%AE%A2%E6%88%B7%E7%AB%AF%E8%BF%9C%E7%A8%8B%E8%B0%83%E7%94%A8%E6%B5%81%E7%A8%8B%E5%9B%BE.png)
---
四. 服务端调用流程图
![image](https://github.com/scanry/dove/blob/master/design/%E6%9C%8D%E5%8A%A1%E7%AB%AF%E6%9C%AC%E5%9C%B0%E8%B0%83%E7%94%A8%E6%B5%81%E7%A8%8B%E5%9B%BE.png)
---
五. 系统整体类图
![image](https://github.com/scanry/dove/blob/master/design/%E7%B3%BB%E7%BB%9F%E6%95%B4%E4%BD%93%E7%B1%BB%E5%9B%BE.png)
---

###系统功能列表
---
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
###系统使用例子
---
一.	通过手动代码使用
---
1.	服务端例子
---
```
DoveServerImpl server = new DoveServerImpl("127.0.0.1", 80);
server.start();
server.register(new TestServiceImpl());
DoveServerTest wait = new DoveServerTest();
synchronized (wait) {
	wait.wait();
}
```
---
2.	客户端例子
---
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
二. 基于spring容器,注解方式使用
---
1.	服务端例子
---
```
@DoveService(protocol = TestService.class,version=Remote.REMOTE_SERVICE_VERSION)
public class TestServiceImpl implements TestService {

	@Override
	public String say(String name) {
		return "hi:" + name;
	}
}
```
---
2. 	客户端例子
---
```
@DoveService(protocol = TestService.class,version=Remote.REMOTE_SERVICE_VERSION,callTimeout=6000,callback="beanId")
private TestService testService;
```
---
三. 基于spring容器,配置方式使用
---
1.	服务端例子
---
```xml
<dove:app name="app" />
<dove:zookeeperRegister id="zookeeperRegister" address="127.0.0.1:2181;127.0.0.1:2182;127.0.0.1:2183" />
<bean id="testService" class="six.com.rpc.TestServiceImpl"/>
<dove:Server  interface="six.com.rpc.TestService" ref="testService" version="${version}" />
```
---
2.	客户端例子
---
```xml
<dove:app name="app" />
<dove:zookeeperRegister id="zookeeperRegister" address="127.0.0.1:2181;127.0.0.1:2182;127.0.0.1:2183" />
<bean id="testServiceCallback" class="six.com.rpc.TestServiceCallback"/>
<dove:client  id="testService" interface="six.com.rpc.TestService" timeout="${timeout}" version="${version}" callback="testServiceCallback" />
```
           