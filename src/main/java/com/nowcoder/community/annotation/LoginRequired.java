package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) //这个注解用在方法之上
@Retention(RetentionPolicy.RUNTIME) //运行时有效
public @interface LoginRequired {
    //起标识作用
}
