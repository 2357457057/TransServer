package top.yqingyu.trans$server.thread;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.trans$server.bean.ClientInfo;
import top.yqingyu.trans$server.command.ParentCommand;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.MsgType;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.S$CtConfig;
import top.yqingyu.trans$server.main.MainConfig;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static top.yqingyu.trans$server.command.ParentCommand.COMMAND;


@SuppressWarnings("all")
@Slf4j
public class DealMsg {

    public QyMsg deal(ChannelHandlerContext ctx, QyMsg qyMsg) throws Exception {

        log.info("DEAL> {}", qyMsg.toString());
        AtomicReference<ParentCommand> a = new AtomicReference<>();

        QyMsg clone;

        try {
            for (int i = 0; i < COMMAND.size(); i++) {
                ParentCommand parentCommand = COMMAND.get(i);
                if (parentCommand.isMatch(MsgHelper.gainMsg(qyMsg))) {
                    a.set(parentCommand);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("命令处理异常", e);
            throw e;
        }

        if (a.get() != null) {
            try {
                ParentCommand parentCommand = a.get();
                ArrayList<QyMsg> qyMsgs = parentCommand.dealCommand(ctx, qyMsg);
                log.info("执行类: {}", parentCommand.getClass().getSimpleName());
                return qyMsgs.get(0);
            } catch (Exception e) {
                clone = MainConfig.ERR_MSG.clone();
                clone.putMsg("error\n$>");
                log.error("命令处理异常", e);
                return clone;
            }
        } else {
            try {
                clone = MainConfig.NORM_MSG.clone();
                clone.putMsg("\n$>");
                return clone;
            } catch (Exception e) {
                log.error("命令处理异常", e);
                throw e;
            }
        }

    }

    public QyMsg deal2(ChannelHandlerContext ctx, QyMsg msg) throws Exception {

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

            return clone;  //告知客户端连接服务器

        } else if (peek != null && "forward".equals(peek.getString("MSG_IN", ""))) {
            clientInfo.getClientInteractionQueue().poll();

            String client = peek.getString("client");
            String port = peek.getString("port");

            QyMsg clone = S$CtConfig.NORM_MSG.clone();
            peek.remove("MSG_IN");
            peek.put("MSG", "forward");
            clone.setDataMap(peek);

            return clone;

        } else { //无队列消息处理

            if (MsgType.HEART_BEAT.equals(msg.getMsgType())) {

                QyMsg clone = S$CtConfig.HEART_BEAT_MSG.clone();
                clone.putMsg(MainConfig.HEART_BEAT);
                return clone;

            } else if ("linked".equals(MsgHelper.gainMsg(msg))) {

                ClientInfo linked = RegistryCenter.getClientInfo(clientInfo.getLinkedClient());
                DataMap dataMap = new DataMap();
                dataMap.put("MSG_OUT", "linked");
                linked.getClientInteractionQueue().add(dataMap);
                log.info("LINK:{} linked {}", clientInfo.getLinkedClient(), msg);

                QyMsg clone = S$CtConfig.HEART_BEAT_MSG.clone();
                clone.putMsg(MainConfig.HEART_BEAT);
                return clone;

            }

        }
        return null;
    }

}
