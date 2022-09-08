package top.yqingyu.component;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.bean.ClientInfo;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.main.MainConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static top.yqingyu.main.MainConfig.ERR_MSG;
import static top.yqingyu.main.MainConfig.MAX_REGISTRY_NUM;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/22 3:14
 * @description 客户端注册中心
 * @modified by
 */
public class RegistryCenter {

    public static final ConcurrentHashMap<String, ClientInfo> REGISTRY_CENTER = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(RegistryCenter.class);


    public static boolean registrationClient(SelectionKey selectionKey, Selector selector, QyMsg msgHeader) throws CloneNotSupportedException {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        try {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();


            InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();


            if (REGISTRY_CENTER.size() < MAX_REGISTRY_NUM) {
                String LAN_Address = MsgHelper.gainMsgValue(msgHeader, "LAN_Address");

                ClientInfo clientInfo = new ClientInfo();
                clientInfo.setClientId(msgHeader.getFrom());
                clientInfo.setSocketChannel(socketChannel);
                clientInfo.setLocalDateTime(LocalDateTime.now());
                clientInfo.setWAN_Address(socketAddress.getHostString());
                clientInfo.setLAN_Address(LAN_Address);


                REGISTRY_CENTER.put(msgHeader.getFrom(), clientInfo);
                return true;
            } else {
                socketChannel.register(selector, SelectionKey.OP_WRITE);

                clone = ERR_MSG.clone();
                clone.putMsg("客户端连接数已达最大值，将会关闭本次链接，请稍后再试！");
                MsgTransfer.writeQyMsg(socketChannel, clone);
                socketChannel.close();
                return false;
            }
        } catch (Exception e) {
            logger.error("异常注册中心异常", e);
            return false;
        }

    }


    public static void removeClient(String userId) throws IOException {
        SocketChannel remove = REGISTRY_CENTER.remove(userId).getSocketChannel();
        remove.shutdownInput();
        remove.shutdownOutput();
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
