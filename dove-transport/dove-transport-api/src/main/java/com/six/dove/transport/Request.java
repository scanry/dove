package com.six.dove.transport;

import com.six.dove.transport.message.MessageProtocol;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @email:359852326@qq.com
 * @version:
 * @describe  请求消息
 */
public class Request extends Message {

    /**
     *
     */
    private static final long serialVersionUID = 3785832578235175559L;

    public Request() {
        super(MessageProtocol.REQUEST);
    }
}
