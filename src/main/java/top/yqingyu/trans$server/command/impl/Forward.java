package top.yqingyu.trans$server.command.impl;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.bean.ClientInfo;
import top.yqingyu.trans$server.command.ParentCommand;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.IoUtil;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.trans$server.component.RegistryCenter;
import top.yqingyu.trans$server.main.MainConfig;
import top.yqingyu.trans$server.thread.ClientTransThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.command.impl.Forward
 * @description
 * @createTime 2022年07月19日 22:30:00
 */
@Slf4j
@Command
public class Forward extends ParentCommand {
    //65535
    private static final String commandRegx = "^(forward)" + "(" + "( ){1,5}" + "((" + "((-)(s|stream))" + "(( ){1,5})([\\w]{8}(-))((([\\w]{4})(-)){3})([\\w]{12})" +          //客户端id
            "(( ){1,5})((6[0-4][\\d]{3})|(65[0-4][\\d]{2})|(655[0-2][\\d])|(6553[0-5])|([1-5][\\d]{4})|([\\d]{0,4}))" +   //端口号
            "(( ){1,5})((6[0-4][\\d]{3})|(65[0-4][\\d]{2})|(655[0-2][\\d])|(6553[0-5])|([1-5][\\d]{4})|([\\d]{0,4}))" + ")" +  // stream
            "|" + "(" + "(-)(stop|help|h)" +  //其他
            "))" + ")?" + "$";


    public Forward() {
        super(commandRegx);
    }

    private static final ThreadPoolExecutor FORWARD_POOL = ThreadUtil.createQyFixedThreadPool(MainConfig.MAX_REGISTRY_NUM * 3, "Fwd", null);

    @Override
    protected void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        StringBuilder sb = new StringBuilder();
        String[] msgSplit = MsgHelper.gainMsg(msg).split(" ");
        if (MsgHelper.gainMsg(msg).matches(commandRegx)) {


            if (msgSplit.length > 1 && msgSplit[1].matches("-(s|stream)")) {


                Pattern clientPatten = Pattern.compile("([\\w]{8}(-))((([\\w]{4})(-)){3})([\\w]{12})");
                Matcher clientMatcher = clientPatten.matcher(MsgHelper.gainMsg(msg));

                if (clientMatcher.find()) {
                    String remote_client_id = clientMatcher.group();


                    String remote_port = msgSplit[3];


                    ClientInfo remote_client = RegistryCenter.getClientInfo(remote_client_id);
                    DataMap dataMap = new DataMap();
                    dataMap.put("MSG_IN", "forward");
                    dataMap.put("client", msg.getFrom());
                    dataMap.put("port", remote_port);
                    remote_client.getClientInteractionQueue().add(dataMap);
                    forwardSocket(msg.getFrom(), remote_client_id);
                } else {
                    sb.append("格式异常");
                }


            } else if (msgSplit.length > 1 && msgSplit[1].matches("-(stop)")) {


            } else if (msgSplit.length > 1 && msgSplit[1].matches("-(h|help)")) {
                sb.append("forward 命令帮助界面\n");
                sb.append(" -help\\h 帮助界面\n");
                sb.append(" 以致于转发数据流\n");
                sb.append(" -stream\\s [clientId] [remote port] [local port] \n");
                sb.append("            被转发的客户端 被转发的客户端port 本地port      \n");
                sb.append("  -stop 停止转发   \n");
                sb.append(" .....\n");


            } else {

                sb.append("forward 命令帮助界面\n");
                sb.append(" -help\\h 帮助界面\n");
                sb.append(" 以致于转发数据流\n");
                sb.append(" -stream\\s [clientId] [remote port] [local port] \n");
                sb.append("            被转发的客户端 被转发的客户端port 本地port      \n");
                sb.append("  -stop 停止转发   \n");
                sb.append(" .....\n");
            }


        }
        sb.append("\n$>");
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg(sb.toString());
        rtnMsg.add(clone);
    }


    void forwardSocket(String client1, String client2) {
        FORWARD_POOL.execute(() -> {
            AtomicReference<Boolean> run = new AtomicReference<>();
            AtomicReference<InputStream> is1 = new AtomicReference<>();
            AtomicReference<OutputStream> os1 = new AtomicReference<>();
            AtomicReference<InputStream> is2 = new AtomicReference<>();
            AtomicReference<OutputStream> os2 = new AtomicReference<>();


            FutureTask<Boolean> getStreamTask = new FutureTask<>(() -> {
                run.set(true);

                Socket socket1 = getSocket(client1);


                if (socket1 != null && socket1.isConnected()) {
                    try {
                        is1.set(socket1.getInputStream());
                        os1.set(socket1.getOutputStream());
                    } catch (IOException e) {
                        log.error("转发流异常1", e);
                        throw new RuntimeException(e);
                    }
                } else {
                    return false;
                }


                Socket socket2 = getSocket(client2);
                if (socket2 != null && socket2.isConnected()) {
                    try {
                        is2.set(socket2.getInputStream());
                        os2.set(socket2.getOutputStream());
                    } catch (IOException e) {
                        log.error("转发流异常2", e);
                        throw new RuntimeException(e);
                    }
                    return true;
                } else {
                    return false;
                }
            });


            FORWARD_POOL.execute(getStreamTask);

            try {
                run.set(getStreamTask.get(32, TimeUnit.SECONDS));
            } catch (Exception e) {
                log.error("取流异常", e);
                return;
            }

            FORWARD_POOL.execute(() -> {
                log.info("转发开始 {}", client1);
                while (run.get()) {
                    try {
                        os2.get().write(IoUtil.readBytes(is1.get(), 1024));
                        os2.get().flush();
                    } catch (Exception e) {
                        run.set(false);
                        log.error("转发停止1", e);
                        return;
                    }
                }
                log.info("转发停止 {}", client1);
            });

            FORWARD_POOL.execute(() -> {
                log.info("转发开始 {}", client2);
                while (run.get()) {
                    try {
                        os1.get().write(IoUtil.readBytes(is2.get(), 1024));
                        os1.get().flush();
                    } catch (Exception e) {
                        run.set(false);
                        log.error("转发停止2", e);
                        return;
                    }
                }
                log.info("转发停止 {}", client2);
            });
        });
    }


    Socket getSocket(String clientId) {

        AtomicReference<Socket> AR_Socket = new AtomicReference<>();
        AtomicReference<Boolean> run = new AtomicReference<>();
        run.set(true);

        FutureTask<Socket> socketFutureTask1 = new FutureTask<Socket>(() -> {

            while (run.get()) {
                Socket socket = ClientTransThread.CLIENT_TRANS_POOL.get(clientId);
                if (socket != null) {
                    AR_Socket.set(socket);
                    break;
                }
                Thread.sleep(1000);
            }
            return AR_Socket.get();
        });

        FORWARD_POOL.execute(socketFutureTask1);

        try {
            AR_Socket.set(socketFutureTask1.get(15, TimeUnit.SECONDS));
        } catch (Exception e) {
            log.error("客户端连接建立异常 client id {}", clientId, e);
            return AR_Socket.get();
        } finally {
            run.set(false);
        }
        return AR_Socket.get();

    }
}
