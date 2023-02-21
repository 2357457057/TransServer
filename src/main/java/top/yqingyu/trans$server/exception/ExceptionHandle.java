package top.yqingyu.trans$server.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.bean.NetChannel;
import top.yqingyu.trans$server.thread.RecordIpThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.trans$server.exception.ExceptionHandle
 * @description
 * @createTime 2023年02月21日 22:22:00
 */
public class ExceptionHandle {

    private final static Logger logger = LoggerFactory.getLogger(ExceptionHandle.class);

    public static void serverExecHandle(Exception e, NetChannel netChannel) {

        if (e instanceof TimeoutException) {
            e.addSuppressed(new Exception("客户端响应超时"));
            logger.error("", e);
            try {
                InetSocketAddress socketAddress = (InetSocketAddress) netChannel.getRemoteAddress();
                RecordIpThread.execute(socketAddress.getHostString());
            } catch (Exception exx) {
                serverExecHandle(exx, netChannel);
            }
            if (netChannel != null) {
                try {
                    netChannel.close();
                } catch (IOException ex) {
                    logger.error("", ex);
                }
            }
        } else if (e instanceof ExecutionException) {
            logger.error("", e);
            if (e.getMessage().matches(".*(NumberFormatException|Cannot.*Boolean[.]booleanValue).*")) {
                try {
                    InetSocketAddress socketAddress = (InetSocketAddress) netChannel.getRemoteAddress();
                    RecordIpThread.execute(socketAddress.getHostString());
                } catch (Exception exx) {
                    serverExecHandle(exx, netChannel);
                }
            }
            if (netChannel != null) {
                try {
                    netChannel.close();
                } catch (IOException ex) {
                    logger.error("", ex);
                }
            }
        } else if (e != null) {
            if (netChannel != null) {
                try {
                    netChannel.close();
                } catch (IOException ex) {
                    logger.error("", ex);
                }
            }
            logger.error("", e);
        }
    }
}
