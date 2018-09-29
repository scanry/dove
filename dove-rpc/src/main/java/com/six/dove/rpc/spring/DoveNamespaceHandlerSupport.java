package com.six.dove.rpc.spring;

import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.six.dove.rpc.DoveServer;
import com.six.dove.rpc.server.DoveServerImpl;

/**
 * @author:MG01867
 * @date:2018年2月2日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class DoveNamespaceHandlerSupport
		implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor, ApplicationContextAware {

	private ApplicationContext applicationContext;
	private DoveConfig doveConfig;
	private DoveServer doveServer;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		this.doveConfig=this.applicationContext.getBean(DoveConfig.class);
		initDoveServer();
	}

	private synchronized void initDoveServer() {
		if (null == doveServer) {
			doveServer = new DoveServerImpl(doveConfig.getHost(), doveConfig.getPort());
			doveServer.start();
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (beanFactory instanceof BeanDefinitionRegistry) {
			try {
				Class<?> scannerClass = Class
						.forName("org.springframework.context.annotation.ClassPathBeanDefinitionScanner");
				Object scanner = scannerClass
						.getConstructor(new Class<?>[] { BeanDefinitionRegistry.class, boolean.class })
						.newInstance(new Object[] { (BeanDefinitionRegistry) beanFactory, true });
				Class<?> filterClass = Class.forName("org.springframework.core.type.filter.AnnotationTypeFilter");
				Object filter = filterClass.getConstructor(Class.class).newInstance(Service.class);
				Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter",
						Class.forName("org.springframework.core.type.filter.TypeFilter"));
				addIncludeFilter.invoke(scanner, filter);
				String[] packages = doveConfig.getScanPackages();
				Method scan = scannerClass.getMethod("scan", new Class<?>[] { String[].class });
				scan.invoke(scanner, new Object[] { packages });
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		doveServer.register(bean);
		return bean;
	}

	@Override
	public void destroy() throws Exception {
		doveServer.stop();
	}

}
