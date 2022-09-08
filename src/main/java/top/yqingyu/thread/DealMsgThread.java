package top.yqingyu.thread;

import lombok.extern.slf4j.Slf4j;
import top.yqingyu.bean.ClientInfo;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.MsgType;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.ClazzUtil;
import top.yqingyu.component.RegistryCenter;
import top.yqingyu.main.S$CtConfig;
import top.yqingyu.main.MainConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("all")
@Slf4j
public record DealMsgThread(SocketChannel socketChannel, Selector selector) {


    private static final HashMap<String, Class> Reg_Classs = new HashMap();




    public void deal(QyMsg msgHeader) throws Exception {

        log.info("DEAL> {}", msgHeader.toString());
        AtomicReference<Class> a = new AtomicReference<>();

        QyMsg clone;

        try {

            if (Reg_Classs.size() == 0) {
                try {
                    List<Class<?>> classList = ClazzUtil.getClassList("top.yqingyu.command.impl", false);

                    for (Class<?> clazz : classList) {
                        Field field = clazz.getDeclaredField("commandRegx");
                        field.setAccessible(true);
                        String o = (String) field.get((Object) null);
                        Reg_Classs.put(o, clazz);
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    log.error("{}", e);
                }
            }

            Reg_Classs.forEach((regx, clazzx) -> {
                if (MsgHelper.gainMsg(msgHeader).matches(regx)) {
                    a.set(clazzx);
                }
            });
        } catch (Exception e) {
            log.error("命令处理异常", e);
            throw e;
        }

        if (a.get() != null) {
            try {
                Class clazz = (Class) a.get();
                Object o = clazz.getDeclaredConstructor().newInstance();
                Method commandDeal = clazz.getMethod("commandDeal", SocketChannel.class, Selector.class, QyMsg.class);
                commandDeal.invoke(o, this.socketChannel, this.selector, msgHeader);
                log.info("调用类: {}", clazz.getName());
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
