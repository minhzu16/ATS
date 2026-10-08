package fptu.sba301.ats.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to automatically log audit events using Spring AOP.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAudit {
    /**
     * The action being performed (e.g., "CREATE", "UPDATE", "DELETE").
     */
    String action();

    /**
     * The resource or entity being affected (e.g., "Department", "Role").
     */
    String resource() default "";
}
