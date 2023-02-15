package top.yqingyu.trans$server.command.impl;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.command.CommandFather;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.thread.DealMsgThread;

import java.lang.reflect.Field;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import static top.yqingyu.trans$server.main.MainConfig.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.command.impl.GetAll
 * @description
 * @createTime 2023年01月08日 16:51:00
 */
@Slf4j
@Command
public class GetAll extends CommandFather {


    public GetAll() {
        super("getcmd");
    }

    @Override
    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        QyMsg clone = NORM_MSG.clone();
        rtnMsg.add(clone);
        Field commands = DealMsgThread.class.getDeclaredField("COMMANDS");
        commands.setAccessible(true);
        ArrayList<CommandFather> o = (ArrayList<CommandFather>)commands.get(null);

        StringBuilder sb = new StringBuilder();
        for (CommandFather commandFather : o) {
            sb.append(commandFather.getCommandRegx()).append("\n");
        }
        clone.putMsg(sb);
    }

}
