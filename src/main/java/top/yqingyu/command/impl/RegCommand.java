package top.yqingyu.command.impl;

import top.yqingyu.command.Command;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.component.RegistryCenter;
import top.yqingyu.main.MainConfig;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/24 4:20
 * @description 注册信息
 * @modified by
 */
public class RegCommand implements Command {

    private static final String commandRegx = "reg.*";


    /**
     * socketChannel.register(selector, SelectionKey.OP_WRITE);
     * <p>
     * socketChannel.register(selector, SelectionKey.OP_READ);
     * description: 命令处理方法
     *
     * @param socketChannel
     * @param selector
     * @param msgHeader
     * @author yqingyu
     * DATE 2022/4/24
     */
    @Override
    public void commandDeal(SocketChannel socketChannel, Selector selector,  QyMsg msgHeader) throws Exception {

        socketChannel.register(selector, SelectionKey.OP_WRITE);


        StringBuilder sb = new StringBuilder();

        if (MsgHelper.gainMsg(msgHeader).matches(commandRegx)) {
            Enumeration<String> keys = RegistryCenter.REGISTRY_CENTER.keys();
            Iterator<String> iterator = keys.asIterator();
            while (iterator.hasNext()) {
                sb.append(iterator.next());
                sb.append("\n");
                sb.append("$>");
            }
            sb.append("total: ");
            sb.append(RegistryCenter.REGISTRY_CENTER.size());
            sb.append("\n");
        }

        sb.append("\n$>");
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg(sb.toString());
        MsgTransfer.writeQyMsg(socketChannel, clone);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }
}
