package me.itzg.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This is used to flag public method that are known to be used externally and therefore should be
 * excluded from "unused symbol" code inspections.
 *
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
public @interface UsedExternally {
}
