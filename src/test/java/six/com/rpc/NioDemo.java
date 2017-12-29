package six.com.rpc;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author sixliu
 * @date 2017年12月27日
 * @email 359852326@qq.com
 * @Description
 */
public class NioDemo {

	public static int id = 100001;
	public static int bufferSize = 2048;

	public static void main(String[] args) throws Exception {
		int port = 8888;
		int pendingAcceptMaxinum = 1024;
		ServerSocketChannel socketChannel = ServerSocketChannel.open();
		Selector selector = Selector.open();
		InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
		socketChannel.socket().bind(inetSocketAddress, pendingAcceptMaxinum);
		// 设置通道非阻塞 绑定选择器
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Server started to listen port:"+8888);
		listener(selector);
	}

	public static void listener(Selector selector) {
		try {
			while (true) {
				selector.select(); // 阻塞 直到有就绪事件为止
				Set<SelectionKey> readySelectionKey = selector.selectedKeys();
				Iterator<SelectionKey> it = readySelectionKey.iterator();
				while (it.hasNext()) {
					SelectionKey selectionKey = it.next();
					// 判断是哪个事件
					if (selectionKey.isAcceptable()) {// 客户请求连接
						System.out.println(selectionKey.attachment() + " - 接受请求事件");
						// 获取通道 接受连接,
						// 设置非阻塞模式（必须），同时需要注册 读写数据的事件，这样有消息触发时才能捕获
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
						serverSocketChannel.accept().configureBlocking(false)
								.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE).attach(id++);
						System.out.println(selectionKey.attachment() + " - 已连接");

						// 下面这种写法是有问题的 不应该在serverSocketChannel上面注册
						/*
						 * serverSocketChannel.configureBlocking(false);
						 * serverSocketChannel.register(in_selector, SelectionKey.OP_READ);
						 * serverSocketChannel.register(in_selector, SelectionKey.OP_WRITE);
						 */
					}
					if (selectionKey.isReadable()) {// 读数据
						System.out.println(selectionKey.attachment() + " - 读数据事件");
						SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
						ByteBuffer receiveBuf = ByteBuffer.allocate(bufferSize);
						clientChannel.read(receiveBuf);
						System.out.println(selectionKey.attachment() + " - 读取数据：" + getString(receiveBuf));
					}
					if (selectionKey.isWritable()) {// 写数据
						System.out.println(selectionKey.attachment() + " - 写数据事件");
						SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
						ByteBuffer sendBuf = ByteBuffer.allocate(bufferSize);
						String sendText = "hello\n";
						sendBuf.put(sendText.getBytes());
						sendBuf.flip(); // 写完数据后调用此方法
						clientChannel.write(sendBuf);
					}
					if (selectionKey.isConnectable()) {
						System.out.println(selectionKey.attachment() + " - 连接事件");
					}
					// 必须removed 否则会继续存在，下一次循环还会进来,
					// 注意removed 的位置，针对一个.next() remove一次
					it.remove();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error - " + e.getMessage());
			e.printStackTrace();
		}

	}

	public static String getString(ByteBuffer buffer) {
		String string = "";
		try {
			for (int i = 0; i < buffer.position(); i++) {
				string += (char) buffer.get(i);
			}
			return string;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

}
