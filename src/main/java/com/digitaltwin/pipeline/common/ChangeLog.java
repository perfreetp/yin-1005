package com.digitaltwin.pipeline.common;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChangeLog {

    String resourceType();

    String operation();

    String module() default "";

    boolean captureDiff() default true;
}
