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

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/24 6:31
 * @description  退出
 * @modified by
 */
public class QuitCommand implements Command {


    private static final String commandRegx = "(quit|exit).*";


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

            sb.append("bye bye!");
            sb.append("\n");
            sb.append("$>");
            sb.append("command_ok");
            sb.append("\n");
            sb.append("$>");

            QyMsg clone = MainConfig.NORM_MSG.clone();
            clone.putMsg(sb.toString());
            MsgTransfer.writeQyMsg(socketChannel,clone);
            socketChannel.register(selector, SelectionKey.OP_READ);


            RegistryCenter.removeClient(msgHeader.getFrom());
        }

    }
}
