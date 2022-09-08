package top.yqingyu.command;

import top.yqingyu.common.qymsg.QyMsg;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/23 22:44
 * @description 所有的命令都必须实现本接口
 * @modified by
 */
public interface Command {


    /**
     * socketChannel.register(selector, SelectionKey.OP_WRITE);
     * <p>
     * socketChannel.register(selector, SelectionKey.OP_READ);
     * description: 命令处理方法
     *
     * @author yqingyu
     * DATE 2022/4/24
     */
    void commandDeal(SocketChannel socketChannel, Selector selector, QyMsg msg) throws Exception;
}
