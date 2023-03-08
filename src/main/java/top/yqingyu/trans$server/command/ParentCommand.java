package top.yqingyu.trans$server.command;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.annotation.Init;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.ClazzUtil;
import top.yqingyu.trans$server.annotation.Command;

import java.lang.reflect.Constructor;
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
public class ParentCommand {


    private static final Logger logger = LoggerFactory.getLogger(ParentCommand.class);
    private final String commandRegx;

    public ParentCommand() {
        commandRegx = "([\n\r]|.)*";
    }

    public ParentCommand(String commandRegx) {
        this.commandRegx = commandRegx;
    }

    public static final ArrayList<ParentCommand> COMMAND = new ArrayList<>();

    @Init
    public void loadCommand() {
        try {
            List<Class<?>> classList = ClazzUtil.getClassListByAnnotation("top.yqingyu.trans$server.command.impl", Command.class);
            for (Class<?> clazz : classList) {
                Constructor<?>[] constructors = clazz.getConstructors();
                if (constructors.length < 1) continue;
                Constructor<ParentCommand> constructor = (Constructor<ParentCommand>) clazz.getConstructor();
                ParentCommand parentCommand = constructor.newInstance();
                COMMAND.add(parentCommand);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        COMMAND.add(new ParentCommand());
    }


    public final ArrayList<QyMsg> dealCommand(ChannelHandlerContext ctx, QyMsg msg) throws Exception {
        ArrayList<QyMsg> rtnMsg = new ArrayList<>(1);
        deal(ctx, msg, rtnMsg);
        addMsgId(rtnMsg, msg);
        return rtnMsg;
    }

    protected void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        QyMsg clone = NORM_MSG.clone();
        clone.putMsg("\n$>");
        rtnMsg.add(clone);
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
