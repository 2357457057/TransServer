package top.yqingyu.trans$server.command.impl;

import com.alibaba.fastjson2.JSONObject;
import top.yqingyu.trans$server.command.Command;
import top.yqingyu.common.qydata.ChoiceHashMap;
import top.yqingyu.common.qydata.ConcurrentQyMap;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.qymsg.MsgHelper;
import top.yqingyu.common.qymsg.QyMsg;
import top.yqingyu.trans$server.main.MainConfig;

import java.lang.reflect.Method;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.command.impl.KeyCommand
 * @description
 * @createTime 2023年01月09日 23:00:00
 */
public class KeyCommand extends Command {

    public KeyCommand() {
        super("key");
    }

    private static final ChoiceHashMap<String, Method> DEAL_MAP = new ChoiceHashMap<>();
    private static final ConcurrentQyMap<String, ConcurrentQyMap<String, Object>> ROOT_MAP = new ConcurrentQyMap<>();

    static {
        Method[] methods = KeyCommand.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().matches("deal.*Key")) {
                method.setAccessible(true);
                DEAL_MAP.put(method.getName().replace("deal", ""), method);
            }
            if ("emptyDeal".equals(method.getName())) {
                method.setAccessible(true);
                DEAL_MAP.putDft("emptyDeal", method);
            }
        }

        ROOT_MAP.put("StringKey", new ConcurrentQyMap<>());

    }

    @Override
    protected void deal(SocketChannel socketChannel, Selector selector, QyMsg msg, ArrayList<QyMsg> rtnMsg) throws Exception {
        DataMap map = msg.getDataMap();
        JSONObject data = map.getJSONObject("key");
        String type = data.getString("KeyType");
        Method method = DEAL_MAP.get(type);
        QyMsg invoke = (QyMsg) method.invoke(this, msg);
        String s = MsgHelper.gainMsg(invoke);
        s+="\n$>";
        invoke.putMsg(s);
        rtnMsg.add(invoke);
    }

    private QyMsg dealStringKey(QyMsg msg) throws CloneNotSupportedException {
        ConcurrentQyMap<String, Object> stringMap = ROOT_MAP.get("StringKey");
        DataMap map = msg.getDataMap();
        JSONObject data = map.getJSONObject("key");
        String type = data.getString("dealType");
        String key = data.getString("key");
        QyMsg clone = MainConfig.NORM_MSG.clone();
        DataMap dataMap = clone.getDataMap();
        JSONObject object = new JSONObject();
        dataMap.put("Key", object);

        switch (type) {
            case "add" -> {
                stringMap.put(key, data.getString("val"));
                object.put(key, data.getString("val"));
                clone.putMsg("success");
                dataMap.put("code", "0000");
            }
            case "get" -> {
                String o = (String) stringMap.get(key);
                object.put(key, o);
                clone.putMsg("success: val: " + o);
                dataMap.put("code", "0000");
                if (o == null) {
                    clone.putMsg("key is expired or not exists");
                    dataMap.put("code", "0000");
                }
            }
            case "rm" -> {
                String o = (String) stringMap.remove(key);
                object.put(key, o);
                clone.putMsg("success");
                dataMap.put("code", "0000");
            }
            default -> {
                clone.putMsg("fail");
                dataMap.put("code", "-1000");
            }
        }
        return clone;
    }


    private QyMsg emptyDeal(QyMsg msg) throws CloneNotSupportedException {
        QyMsg clone = MainConfig.NORM_MSG.clone();
        clone.putMsg("尚未支持的数据类型");
        DataMap dataMap = clone.getDataMap();
        dataMap.put("code", "-1000");
        JSONObject object = new JSONObject();
        dataMap.put("Key", object);
        return clone;

    }
}
