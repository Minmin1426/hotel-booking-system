package com.hotelbooking.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resolves the currently authenticated user's ID (userId) into a controller method parameter.
 * Used together with {@link CurrentUserArgumentResolver}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}