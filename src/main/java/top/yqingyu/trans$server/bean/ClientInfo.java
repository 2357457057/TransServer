package top.yqingyu.trans$server.bean;


import com.alibaba.fastjson2.JSON;
import top.yqingyu.common.qydata.DataMap;

import java.io.Serializable;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/4/23 22:44
 * @description 客户端信息
 * @modified by
 */
public class ClientInfo implements Serializable {

    private String clientId;
    private SocketChannel socketChannel;

    //心跳时间
    private LocalDateTime localDateTime;

    //公网地址
    private String WAN_Address;

    //局域网地址
    private String LAN_Address;

    private boolean linking = false;

    private Socket socket;

    private String linkedClient;

    private volatile String currentPath = "~/";


    //客户端交互信息
    private final ConcurrentLinkedQueue<DataMap> ClientInteractionQueue = new ConcurrentLinkedQueue<>();

    public ClientInfo() {
    }

    public ClientInfo(String clientId, SocketChannel socketChannel, LocalDateTime localDateTime, String WAN_Address, String LAN_Address) {
        this.clientId = clientId;
        this.socketChannel = socketChannel;
        this.localDateTime = localDateTime;
        this.WAN_Address = WAN_Address;
        this.LAN_Address = LAN_Address;
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public String getWAN_Address() {
        return WAN_Address;
    }

    public void setWAN_Address(String WAN_Address) {
        this.WAN_Address = WAN_Address;
    }

    public String getLAN_Address() {
        return LAN_Address;
    }

    public void setLAN_Address(String LAN_Address) {
        this.LAN_Address = LAN_Address;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public ConcurrentLinkedQueue<DataMap> getClientInteractionQueue() {
        return ClientInteractionQueue;
    }

    public boolean isLinking() {
        return linking;
    }

    public void setLinking(boolean linking) {
        this.linking = linking;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getLinkedClient() {
        return linkedClient;
    }

    public void setLinkedClient(String linkedClient) {
        this.linkedClient = linkedClient;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }
}
