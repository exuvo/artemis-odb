package com.artemis.annotations;

import com.artemis.*;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Auto-configures fields pertaining to aspects. The annotated field
 * must be one the following types: {@link Aspect}, {@link Aspect.Builder},
 * {@link EntitySubscription}, {@link EntityTransmuter}.</p>
 * 
 * <p>This annotation can be combined with {@link All} and {@link One}, 
 * but will be ignored if {@link AspectDescriptor} is present.</p>
 * 
 * <p>This annotation works similar to {@link Wire}; fields are configured
 * during {@link EntitySystem#initialize()}, or explicitly via {@link World#inject(Object)}.</p>
 * 
 * <h4>Note on EntityTransmuters</h4>
 * <p>{@link #value()} corresponds to remove.</p>
 * 
 * @see All
 * @see One
 * @see AspectDescriptor
 * @see Wire
 * 
 * @author Ken Schosinsky
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
public @interface Exclude {

	/**
	 * @return excluding types
	 */
	Class<? extends Component>[] value() default {};

}
