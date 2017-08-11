package six.com.rpc.client;


public enum FutureState {
	/**
	 * 正在处理
	 */
	DOING(0),
	/**
	 * 已经完成
	 */
	DONE(1),
	/**
	 * 被取消
	 */
	CANCELLED(2);

	public final int value;

	private FutureState(int value) {
		this.value = value;
	}

	public boolean isCancelledState() {
		return this == CANCELLED;
	}

	public boolean isDoneState() {
		return this == DONE;
	}

	public boolean isDoingState() {
		return this == DOING;
	}
}
