package top.yqingyu.trans$server.command;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.annotation.Init;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.common.utils.ClazzUtil;
import top.yqingyu.trans$server.annotation.Command;
import top.yqingyu.trans$server.bean.Bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static top.yqingyu.trans$server.main.MainConfig.*;

/**
 * @author YYJ
 * @version 1.0.0
 * @description 所有的命令都必须实现本接口
 */
@Init
@Command
public class ParentCommand {
    private static final Logger logger = LoggerFactory.getLogger(ParentCommand.class);

    public static final ArrayList<Bean> COMMAND = new ArrayList<>();

    @Init
    public void loadCommand() throws NoSuchMethodException {
        try {
            List<Class<?>> classList = ClazzUtil.getClassListByAnnotation("top.yqingyu.trans$server.command.impl", Command.class);
            for (Class<?> clazz : classList) {
                Constructor<?>[] constructors = clazz.getConstructors();
                if (constructors.length < 1) continue;
                Constructor<?> constructor = clazz.getConstructor();
                Command classAnno = clazz.getAnnotation(Command.class);
                String value = classAnno.value();
                Object o = constructor.newInstance();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    Command annotation = method.getAnnotation(Command.class);
                    if (annotation == null) continue;
                    String s = annotation.value();
                    method.setAccessible(true);
                    COMMAND.add(new Bean(method, Pattern.compile(value + s), o));
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        Method deal = ParentCommand.class.getDeclaredMethod("deal", ChannelHandlerContext.class, QyMsg.class, ArrayList.class);
        COMMAND.add(new Bean(deal, Pattern.compile("([\n\r]|.)*"), new ParentCommand()));
    }

    @Command
    public void deal(ChannelHandlerContext ctx, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        QyMsg clone = NORM_MSG.clone();
        clone.putMsg("$>");
        rtnMsg.add(clone);
    }
}
