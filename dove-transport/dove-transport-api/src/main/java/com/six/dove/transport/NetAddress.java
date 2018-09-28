package com.six.dove.transport;

import java.util.Objects;

/**
 * @author:MG01867
 * @date: 2018年5月9日
 * @email: 359852326@qq.com
 * @version:
 * @describe 网络地址
 */
public class NetAddress {

    private String host;

    private int port;

    public NetAddress(String host, int port) {
        Objects.requireNonNull(host);
        if (port <= 0) {
            throw new IllegalArgumentException(String.format("The port[%s] must greater than 0", port));
        }
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return host.hashCode() + port;
    }
}
