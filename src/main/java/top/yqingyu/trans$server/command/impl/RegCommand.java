package top.yqingyu.trans$server.command.impl;

import top.yqingyu.trans$server.command.Command;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/24 4:20
 * @description 注册信息
 * @modified by
 */
public class RegCommand extends Command {

    private static final String commandRegx = "reg.*";

    public RegCommand() {
        super(commandRegx);
    }

    @Override
    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
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
