package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.command.ParentCommand;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;

import java.util.ArrayList;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/24 6:31
 * @description 退出
 * @modified by
 */
@Command
public class Quit extends ParentCommand {


    private static final String commandRegx = "(quit|exit).*";

    public Quit() {
        super(commandRegx);
    }

    @Override
    protected  void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {

        StringBuilder sb = new StringBuilder();
            sb.append("bye bye!");
            sb.append("\n");
            sb.append("$>");
            sb.append("command_ok");
            sb.append("\n");
            sb.append("$>");
            QyMsg clone = MainConfig.NORM_MSG.clone();
            clone.putMsg(sb.toString());
            RegistryCenter.removeClient(msg.getFrom());
            rtnMsg.add(clone);

    }
}
