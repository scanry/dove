package com.six.dove.common;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractService implements Service {

	private static Logger log = LoggerFactory.getLogger(AbstractService.class);

	private static AtomicReferenceFieldUpdater<AbstractService, State> STATE_UPDATE = AtomicReferenceFieldUpdater
			.newUpdater(AbstractService.class, State.class, "state");
	private final String name;
	private volatile State state = State.INIT;

	public AbstractService(String name) {
		Objects.requireNonNull(name);
		this.name = name;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final State getState() {
		return state;
	}

	@Override
	public final boolean isRunning() {
		return State.START == state;
	}

	@Override
	public final void start() {
		if (STATE_UPDATE.compareAndSet(this, State.INIT, State.START)) {
			log.info("will start service[" + getName() + "]");
			doStart();
			log.info("started service[" + getName() + "]");
		}
	}

	protected abstract void doStart();

	@Override
	public final void stop() {
		if (STATE_UPDATE.compareAndSet(this, State.START, State.STOP)) {
			log.info("will stop service[" + getName() + "]");
			doStop();
			log.info("stoped service[" + getName() + "]");
		}
	}

	protected abstract void doStop();
}
