package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;

import java.util.ArrayList;

/**
 * @author YYJ
 * @version 1.0.0
 * @description 退出
 */
public class Quit {

    private static final String commandRegx = "(quit|exit).*";

    @Command(commandRegx)
    protected void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {

        String sb = """
                bye bye!
                $>command_ok
                $>""";
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg(sb);
        RegistryCenter.removeClient(msg.getFrom());
        rtnMsg.add(clone);

    }
}
