package top.yqingyu.command;

import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.main.MainConfig;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/23 22:44
 * @description 所有的命令都必须实现本接口
 * @modified by
 */
public class Command {


    private final String commandRegx;

    public Command() {
        commandRegx = "([\n\r]|.)*";
    }

    public Command(String commandRegx) {
        this.commandRegx = commandRegx;
    }


    public final void dealCommand(SocketChannel socketChannel, Selector selector, QyMsg msg) throws Exception {
        ArrayList<QyMsg> rtnMsg = new ArrayList<>(1);
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        deal(socketChannel, selector, msg, rtnMsg);
        writeMsg(socketChannel, rtnMsg);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg("\n$>");
        rtnMsg.add(clone);
    }

    /**
     * 向外写出消息 可选择重写
     *
     * @description
     */
    public void writeMsg(SocketChannel socketChannel, List<QyMsg> msgList) throws Exception {
        for (QyMsg msg : msgList) {
            MsgTransfer.writeQyMsg(socketChannel, msg);
        }
    }

    public boolean isMatch(String command) {
        if (command == null) {
            return false;
        }
        return command.matches(this.commandRegx);
    }

    public String getCommandRegx() {
        return commandRegx;
    }
}
