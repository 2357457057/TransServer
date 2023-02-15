package top.yqingyu.trans$server.annotation;

import top.yqingyu.common.annotation.Init;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Init
public @interface Command {
}
