package top.yqingyu.command.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.bean.ClientInfo;
import top.yqingyu.command.Command;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.component.RegistryCenter;
import top.yqingyu.main.MainConfig;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 1024; i++) {
            sb.append("12345678901234567890");
        }
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg(sb.toString());
        MsgTransfer.writeQyMsg(socketChannel, clone);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }
}
