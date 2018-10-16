package com.six.dove.transport;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @email:359852326@qq.com
 * @version:
 * @describe 传输层连接接口
 */
public interface Connection<SendMsg extends Message> extends AutoCloseable {

    /**
     * 获取连接远程地址
     *
     * @return 连接远程地址
     */
    NetAddress getNetAddress();

    /**
     * 是否可用
     *
     * @return 是否可用
     */
    boolean available();
    
    
    /**
     * 是否关闭
     *
     * @return 是否关闭
     */
    boolean closed();

    /**
     * 获取最近活动时间
     *
     * @return 最近活动时间
     */
    long getLastActivityTime();

    /**
     * 空监听
     */
    SendListener sendListener = future -> {
    };

    /**
     * 发送消息
     *
     * @param message 发送消息
     */
    default void send(SendMsg message) {
        send(message, sendListener);
    }

    /**
     * 发送消息 并监听发送结果
     *
     * @param message  发送消息
     * @param listener 发送结果监听
     */
    void send(SendMsg message, SendListener listener);

    /**
     * 连接关闭
     */
    void close();

    /**
     * 发送消息监听
     */
    @FunctionalInterface
    interface SendListener {
        void complete(SendFuture future);
    }

    /**
     * 发送future
     */
    interface SendFuture {
        boolean isSucceed();
    }
}
