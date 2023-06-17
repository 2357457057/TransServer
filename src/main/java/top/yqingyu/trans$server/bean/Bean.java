package top.yqingyu.trans$server.bean;

import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.common.qymsg.QyMsg;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Bean {
    private final Method method;
    private final Pattern pattern;
    private final Object target;

    public Bean(Method method, Pattern pattern, Object target) {
        this.method = method;
        this.pattern = pattern;
        this.target = target;
    }

    public boolean match(String match) {
        return pattern.matcher(match).matches();
    }

    public ArrayList<QyMsg> invoke(ChannelHandlerContext ctx, QyMsg msg) throws ReflectiveOperationException {
        ArrayList<QyMsg> list = new ArrayList<>(1);
        method.invoke(target, ctx, msg, list);
        addMsgId(list, msg);
        return list;
    }

    private void addMsgId(List<QyMsg> rtnMsg, QyMsg msg) {
        String msgId = msg.gainMsgId();
        for (QyMsg qyMsg : rtnMsg) {
            qyMsg.setDataType(msg.getDataType());
            qyMsg.putMsgId(msgId);
        }
    }

    public String getRegex() {
        return pattern.pattern();
    }
}
