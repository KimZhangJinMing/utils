package com.example.cybs.util.annotation;


import java.lang.annotation.*;


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CyberSourceProperty {
    String value() default "";
}
