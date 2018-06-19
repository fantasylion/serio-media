package com.serio.core.annotation.media;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zl.shi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FfmpegOption {

	public String value() default "";
	
	public OptionType type() default OptionType.EMPTY;
	
	public enum OptionType{ INPUT, OUTPUT, GLOBAL, EMPTY }
}
