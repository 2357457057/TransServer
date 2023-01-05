package top.yqingyu.command.impl;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.command.Command;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.main.MainConfig;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.command.impl.QueueCommand
 * @description
 * @createTime 2022年09月07日 10:36:00
 */
@Slf4j
public class QueueCommand implements Command {

    private static final String commandRegx = "getMsg";
    @Override
    public void commandDeal(SocketChannel socketChannel, Selector selector, QyMsg msg) throws Exception {
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg("12345678901234567890".repeat(10240));
        MsgTransfer.writeQyMsg(socketChannel, clone);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }
}
