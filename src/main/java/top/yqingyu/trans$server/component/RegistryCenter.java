package top.yqingyu.trans$server.component;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.trans$server.bean.ClientInfo;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.main.MainConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YYJ
 * @version 1.0.0
 * @description 客户端注册中心
 */
public class RegistryCenter {

    public static final ConcurrentHashMap<String, ClientInfo> REGISTRY_CENTER = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(RegistryCenter.class);


    public static boolean registrationClient(ChannelHandlerContext ctx, QyMsg msg) throws CloneNotSupportedException {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        try {

            Channel channel = ctx.channel();
            InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
            if (!MainConfig.AC_STR.equals(MsgHelper.gainMsgValue(msg, "AC_STR"))) {
                return false;
            }

            if (REGISTRY_CENTER.size() < MainConfig.MAX_REGISTRY_NUM) {
                String LAN_Address = MsgHelper.gainMsgValue(msg, "LAN_Address");

                ClientInfo clientInfo = new ClientInfo();
                clientInfo.setClientId(msg.getFrom());
                clientInfo.setCtx(ctx);
                clientInfo.setLocalDateTime(LocalDateTime.now());
                clientInfo.setWAN_Address(socketAddress.getHostString());
                clientInfo.setLAN_Address(LAN_Address);


                REGISTRY_CENTER.put(msg.getFrom(), clientInfo);
                return true;
            } else {
//                clone = MainConfig.ERR_MSG.clone();
//                clone.putMsg("客户端连接数已达最大值，将会关闭本次链接，请稍后再试！");
//                MsgTransfer.writeQyMsg(socketChannel, clone);
                ctx.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("异常注册中心异常", e);
            return false;
        }

    }


    public static void removeClient(String userId) throws IOException {
        Channel remove = REGISTRY_CENTER.remove(userId).getCtx().channel();
        remove.close();

    }


    public static boolean isRegistered(String userId) throws IOException {

        ClientInfo clientInfo = REGISTRY_CENTER.get(userId);
        if (clientInfo != null) {
            clientInfo.setLocalDateTime(LocalDateTime.now());
            return true;
        }
        return false;
    }

    public static ClientInfo getClientInfo(String userId) throws IOException {
        return REGISTRY_CENTER.get(userId);
    }

}
