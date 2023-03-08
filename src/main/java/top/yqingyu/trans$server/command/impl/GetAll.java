package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.command.ParentCommand;
import top.yqingyu.common.qymsg.QyMsg;

import java.lang.reflect.Field;
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
public class GetAll extends ParentCommand {


    public GetAll() {
        super("getcmd");
    }

    @Override
    protected void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        QyMsg clone = NORM_MSG.clone();
        rtnMsg.add(clone);
        Field commands = ParentCommand.class.getDeclaredField("COMMAND");
        commands.setAccessible(true);
        ArrayList<ParentCommand> o = (ArrayList<ParentCommand>)commands.get(null);

        StringBuilder sb = new StringBuilder();
        for (ParentCommand parentCommand : o) {
            sb.append(parentCommand.getCommandRegx()).append("\n");
        }
        clone.putMsg(sb);
    }

}
