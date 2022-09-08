package top.yqingyu.command.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.bean.ClientInfo;
import top.yqingyu.command.Command;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.MsgTransfer;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.component.RegistryCenter;
import top.yqingyu.main.MainConfig;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.command.impl.LinkCommand
 * @Description 链接
 * @createTime 2022年05月08日 14:32:00
 */
@Slf4j
public class LinkCommand implements Command {

    private static final String commandRegx = "^(link)(( )((-)([lhseip]{1,5}|show|help|link)(( )([\\u4e00-\\u9fa5\\w/.\\\\_-]{0,200}))?)|([\\u4e00-\\u9fa5\\w/.\\\\_]{0,200}))?$";

    /**
     * socketChannel.register(selector, SelectionKey.OP_WRITE);
     * <p>
     * socketChannel.register(selector, SelectionKey.OP_READ);
     * description: 命令处理方法
     *
     * @param socketChannel
     * @param selector
     * @param msgHeader
     * @author yqingyu
     * DATE 2022/05/08
     */
    @Override
    public void commandDeal(SocketChannel socketChannel, Selector selector,  QyMsg msgHeader) throws Exception {
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        StringBuilder sb = new StringBuilder();

        if (MsgHelper.gainMsg(msgHeader).matches(commandRegx)) {
            String[] msgSplit = MsgHelper.gainMsg(msgHeader).split(" ");

            if (msgSplit.length == 2) {
                if (msgSplit[1].contains("-")) {
                    if (msgSplit[1].matches("-(show|s)")) {
                        sb.append("目前在线的客户端数量：");
                        sb.append(RegistryCenter.REGISTRY_CENTER.size());
                        sb.append("\n");
                        sb.append("本机id：");
                        sb.append(msgHeader.getFrom());
                        sb.append("\n");
                        sb.append("$> id                                   外网ip\t\t本网ip\t\t心跳时间\n");
                        RegistryCenter.REGISTRY_CENTER.forEach((id, clientInfo) -> {
                            sb.append("$> ");
                            sb.append(id);
                            sb.append("\t");
                            sb.append(clientInfo.getWAN_Address());
                            sb.append("\t");
                            sb.append(clientInfo.getLAN_Address());
                            sb.append("\t");
                            sb.append(LocalDateTimeUtil.format(clientInfo.getLocalDateTime(), "yyyy-MM-dd HH:mm:ss.SSS"));
                            sb.append("\n");
                        });
                    }

                    if (msgSplit[1].matches("-(help|h)")) {
                        sb.append("link 命令帮助界面\n");
                        sb.append(" -help\\h 帮助界面\n");
                        sb.append(" -link\\l {id} 链接到另一台主机\n");
                        sb.append(" -show\\s 查看当前在线主机信息\n");
                        sb.append(" .....\n");
                    }

                } else {
                    sb.append("格式有误\n");
                }
            } else if (msgSplit.length == 3) {
                if (msgSplit[1].contains("-")) {
                    if (msgSplit[1].matches("-(link|l)")) {
                        ClientInfo remoteClient = RegistryCenter.getClientInfo(msgSplit[2].trim());
                        if (remoteClient != null) {
                            DataMap data = new DataMap();
                            data.put("MSG_IN","link");
                            data.put("link_id",msgHeader.getFrom());
                            remoteClient.getClientInteractionQueue().add(data);

                            ClientInfo localClient = RegistryCenter.getClientInfo(msgHeader.getFrom());
                            ConcurrentLinkedQueue<DataMap> clientInteractionQueue = localClient.getClientInteractionQueue();

                            boolean[] run = {true};
                            FutureTask<String> futureTask = new FutureTask<>(() -> {

                                while (run[0]) {
                                    DataMap peek = clientInteractionQueue.peek();
                                    if (peek != null) {
                                        if ("linked".equals(peek.getString("MSG_OUT",""))) {
                                            clientInteractionQueue.remove();
                                            break;
                                        }
                                    }
                                    Thread.sleep(3000);
                                }
                                return remoteClient.getClientId() + " 连接成功\n";
                            });

                            Thread thread = new Thread(futureTask);
                            thread.start();

                            try {
                                sb.append(futureTask.get(10, TimeUnit.SECONDS));
                            } catch (TimeoutException e) {
                                run[0] = false;
                                log.info("linked连接超时 客户端ip:{} |E：{}", remoteClient.getClientId(), e);
                                sb.append(remoteClient.getClientId());
                                sb.append(" 连接超时\n");
                            }
                        } else {
                            sb.append(" 该客户端不在线\n");
                        }
                    }
                } else {
                    sb.append("格式有误\n");
                    log.info("link 格式不对");
                }
            } else {
                sb.append("格式有误\n");
                log.info("link 格式不对");
            }


        } else {

            sb.append("格式有误\n");
            log.info("不可能跑到这！！！！！");
        }
        sb.append("\n$>");
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg(sb.toString());
        MsgTransfer.writeQyMsg(socketChannel, clone);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }
}
