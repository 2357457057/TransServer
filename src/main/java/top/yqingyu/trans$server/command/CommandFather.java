package top.yqingyu.trans$server.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.annotation.Init;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.ClazzUtil;
import top.yqingyu.trans$server.annotation.Command;

import java.lang.reflect.Constructor;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static top.yqingyu.trans$server.main.MainConfig.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/23 22:44
 * @description 所有的命令都必须实现本接口
 * @modified by
 */
@Command
public class CommandFather {


    private static final Logger logger = LoggerFactory.getLogger(CommandFather.class);
    private final String commandRegx;

    public CommandFather() {
        commandRegx = "([\n\r]|.)*";
    }

    public CommandFather(String commandRegx) {
        this.commandRegx = commandRegx;
    }

    public static final ArrayList<CommandFather> COMMAND = new ArrayList<>();

    @Init
    public void loadCommand() {
        try {
            List<Class<?>> classList = ClazzUtil.getClassListByAnnotation("top.yqingyu.trans$server.command", Command.class);
            for (Class<?> clazz : classList) {
                Constructor<?>[] constructors = clazz.getConstructors();
                if (constructors.length < 1) continue;
                Constructor<CommandFather> constructor = (Constructor<CommandFather>) clazz.getConstructor();
                CommandFather commandFather = constructor.newInstance();
                COMMAND.add(0, commandFather);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }


    public final void dealCommand(SocketChannel socketChannel, Selector selector, QyMsg msg) throws Exception {
        ArrayList<QyMsg> rtnMsg = new ArrayList<>(1);
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        deal(socketChannel, selector, msg, rtnMsg);
        addMsgId(rtnMsg, msg);
        writeMsg(socketChannel, rtnMsg);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        QyMsg clone = NORM_MSG.clone();
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

    public final boolean isMatch(String command) {
        if (command == null) {
            return false;
        }
        return command.matches(this.commandRegx);
    }

    public final String getCommandRegx() {
        return commandRegx;
    }

    void addMsgId(List<QyMsg> rtnMsg, QyMsg msg) {
        String msgId = msg.gainMsgId();
        for (QyMsg qyMsg : rtnMsg) {
            qyMsg.putMsgId(msgId);
        }

    }
}
