package top.yqingyu.trans$server.command.impl;

import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.command.ParentCommand;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

@Command
public class Download extends ParentCommand {
    public Download() {
        super("download");
    }

    @Override
    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {

    }
}

