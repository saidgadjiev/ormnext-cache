package ru.saidgajiev.ormnext.cache;

import ru.saidgajiev.ormnext.cache.policy.CachePut;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate entity classes with this annotation for caching.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {

    CachePut[] policies() default {};
}
