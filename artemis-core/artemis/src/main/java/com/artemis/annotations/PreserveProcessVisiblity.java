package com.artemis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.artemis.systems.IteratingSystem;

/**
 * When optimizing an {@link IteratingSystem}, don't reduce the visibility
 * of {@link IteratingSystem#process()}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface PreserveProcessVisiblity {}
