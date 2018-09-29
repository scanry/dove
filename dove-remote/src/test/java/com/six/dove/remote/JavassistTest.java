package com.six.dove.remote;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.six.dove.remote.client.AbstractClientRemote;


/**
 * @author:MG01867
 * @date:2018年5月9日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class JavassistTest {

	@Test
	public void test() throws Exception {
		doTest();
	}
	
	interface Service{
		public void test1() throws Exception;
		public String test1(String name) throws Exception;
		public int test1(String name,int sex) throws Exception;
	}
	
	public String doTest() throws Exception {
		com.six.dove.remote.DoveContext.getCurrentThreadAsyCallback();
		List<String> proxyMethodCodes=AbstractClientRemote.proxyMethodCodes(Service.class);
		for(String proxyMethodCode:proxyMethodCodes) {
			System.out.println(proxyMethodCode);
		}
		return null;
	}
}
