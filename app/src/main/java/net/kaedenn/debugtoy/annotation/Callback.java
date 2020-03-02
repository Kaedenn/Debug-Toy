package net.kaedenn.debugtoy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** The annotated function is called via some {@code onClick} event.
 *
 * This annotation is used to specify that the annotated method is called via
 * some sort of {@code View} action, and not via code contained within the main
 * application.
 *
 * This annotation specifies no behavior and mandates nothing; it's provided
 * only to aid code maintenance.
 */
@Target(ElementType.METHOD)
public @interface Callback {
}
