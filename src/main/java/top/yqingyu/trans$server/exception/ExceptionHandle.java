package top.yqingyu.trans$server.exception;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.exception.IllegalQyMsgException;
import top.yqingyu.common.qymsg.netty.ServerExceptionHandler;
import top.yqingyu.trans$server.thread.RecordIpThread;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.trans$server.exception.ExceptionHandle
 * @description
 * @createTime 2023年02月21日 22:22:00
 */
public class ExceptionHandle implements ServerExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(ExceptionHandle.class);


    @Override
    public void handle(ChannelHandlerContext ctx, Throwable cause) {
        String causeMessage = cause.getMessage();
        if (cause instanceof IllegalQyMsgException) {
            RecordIpThread.execute(((InetSocketAddress) ctx.channel().remoteAddress()).getHostString());
            logger.warn("已知异常 {} ", causeMessage, cause);
        } else if (cause instanceof SocketException && "Connection reset".equals(causeMessage)) {
            logger.debug("链接重置 {}", ctx.hashCode());
        } else if (cause instanceof DecoderException) {
            RecordIpThread.execute(((InetSocketAddress) ctx.channel().remoteAddress()).getHostString());
            logger.warn("已知异常 {} ", causeMessage, cause);
            ctx.close();
        } else {
            serverExecHandle(cause, ctx);
        }
    }

    public static void serverExecHandle(Throwable e, ChannelHandlerContext ctx) {
        if (e instanceof TimeoutException) {
            e.addSuppressed(new Exception("业务处理超时"));
            logger.error("", e);
            try {
                InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                RecordIpThread.execute(socketAddress.getHostString());
            } catch (Exception exx) {
                serverExecHandle(exx, ctx);
            }
            if (ctx != null) {
                ctx.close();
            }
        } else if (e instanceof ExecutionException) {
            logger.error("线程池异常", e);
            if (e.getMessage().matches(".*(NumberFormatException|Cannot.*Boolean[.]booleanValue).*")) {
                try {
                    InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                    RecordIpThread.execute(socketAddress.getHostString());
                } catch (Exception exx) {
                    serverExecHandle(exx, ctx);
                }
            }
            if (ctx != null) {
                ctx.close();
            }
        } else if (e != null) {

            logger.error("未知异常 {}", e.getMessage(), e);
        }
    }
}
