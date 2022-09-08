package top.yqingyu.command.impl;

import top.yqingyu.command.Command;
import top.yqingyu.common.qymsg.QyMsg;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.command.impl.QueueCommand
 * @description
 * @createTime 2022年09月07日 10:36:00
 */
public class QueueCommand implements Command {

    private static final String commandRegx = "jfaslkdal";
    @Override
    public void commandDeal(SocketChannel socketChannel, Selector selector, QyMsg msg) throws Exception {

    }
}
