package top.yqingyu.trans$server.thread;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.trans$server.bean.ClientInfo;
import top.yqingyu.trans$server.command.CommandFather;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.MsgType;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.S$CtConfig;
import top.yqingyu.trans$server.main.MainConfig;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicReference;
import static top.yqingyu.trans$server.command.CommandFather.COMMAND;


@SuppressWarnings("all")
@Slf4j
public record DealMsgThread(SocketChannel socketChannel, Selector selector) {

    public void deal(QyMsg qyMsg) throws Exception {

        log.info("DEAL> {}", qyMsg.toString());
        AtomicReference<CommandFather> a = new AtomicReference<>();

        QyMsg clone;

        try {
            for (int i = 0; i < COMMAND.size(); i++) {
                CommandFather commandFather = COMMAND.get(i);
                if (commandFather.isMatch(MsgHelper.gainMsg(qyMsg))) {
                    a.set(commandFather);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("命令处理异常", e);
            throw e;
        }

        if (a.get() != null) {
            try {
                CommandFather commandFather = a.get();
                commandFather.dealCommand(this.socketChannel, this.selector, qyMsg);
                log.info("执行类: {}", commandFather.getClass().getSimpleName());
            } catch (Exception e) {
                this.socketChannel.register(this.selector, SelectionKey.OP_WRITE);

                clone = MainConfig.ERR_MSG.clone();
                clone.putMsg("error\n$>");

                MsgTransfer.writeQyMsg(this.socketChannel, clone);
                this.socketChannel.register(this.selector, SelectionKey.OP_READ);
                log.error("命令处理异常", e);
                throw e;
            }
        } else {
            try {
                this.socketChannel.register(this.selector, SelectionKey.OP_WRITE);

                clone = MainConfig.NORM_MSG.clone();
                clone.putMsg("\n$>");
                MsgTransfer.writeQyMsg(this.socketChannel, clone);
                this.socketChannel.register(this.selector, SelectionKey.OP_READ);
            } catch (Exception e) {
                log.error("命令处理异常", e);
                throw e;
            }
        }

    }

    public void deal2(QyMsg msg) throws Exception {

        socketChannel.register(selector, SelectionKey.OP_WRITE);


        ClientInfo clientInfo = RegistryCenter.getClientInfo(msg.getFrom());

        DataMap peek = clientInfo.getClientInteractionQueue().peek();

        //有列消息处理
        if (peek != null && "link".equals(peek.getString("MSG_IN", ""))) {

            ClientInfo linked = RegistryCenter.getClientInfo(peek.getString("link_id"));

            linked.setLinkedClient(msg.getFrom());
            clientInfo.getClientInteractionQueue().poll();
            clientInfo.setLinkedClient(peek.getString("link_id"));


            QyMsg clone = S$CtConfig.NORM_MSG.clone();
            peek.remove("MSG_IN");
            peek.put("MSG", "link");

            MsgTransfer.writeQyMsg(socketChannel, clone);  //告知客户端连接服务器

        } else if (peek != null && "forward".equals(peek.getString("MSG_IN", ""))) {
            clientInfo.getClientInteractionQueue().poll();

            String client = peek.getString("client");
            String port = peek.getString("port");

            QyMsg clone = S$CtConfig.NORM_MSG.clone();
            peek.remove("MSG_IN");
            peek.put("MSG", "forward");
            clone.setDataMap(peek);

            MsgTransfer.writeQyMsg(socketChannel, clone);

        } else { //无队列消息处理

            if (MsgType.HEART_BEAT.equals(msg.getMsgType())) {

                QyMsg clone = S$CtConfig.HEART_BEAT_MSG.clone();
                clone.putMsg(MainConfig.HEART_BEAT);
                MsgTransfer.writeQyMsg(socketChannel, clone);

            } else if ("linked".equals(MsgHelper.gainMsg(msg))) {

                ClientInfo linked = RegistryCenter.getClientInfo(clientInfo.getLinkedClient());
                DataMap dataMap = new DataMap();
                dataMap.put("MSG_OUT", "linked");
                linked.getClientInteractionQueue().add(dataMap);
                log.info("LINK:{} linked {}", clientInfo.getLinkedClient(), msg);

                QyMsg clone = S$CtConfig.HEART_BEAT_MSG.clone();
                clone.putMsg(MainConfig.HEART_BEAT);
                MsgTransfer.writeQyMsg(socketChannel, clone);

            }


        }
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public SocketChannel socketChannel() {
        return this.socketChannel;
    }

    public Selector selector() {
        return this.selector;
    }
}
