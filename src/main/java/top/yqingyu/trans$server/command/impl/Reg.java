package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author YYJ
 * @version 1.0.0
 * @description 注册信息
 */
@Command
public class Reg  {

    private static final String commandRegx = "reg.*";

    @Command(commandRegx)
    protected void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        StringBuilder sb = new StringBuilder();

        if (MsgHelper.gainMsg(msg).matches(commandRegx)) {
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
        rtnMsg.add(clone);
    }
}
