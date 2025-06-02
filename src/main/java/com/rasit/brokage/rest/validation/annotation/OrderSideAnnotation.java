package com.rasit.brokage.rest.validation.annotation;

import com.rasit.brokage.rest.validation.OrderSideAnnotationValidator;
import com.rasit.brokage.utility.BrokageConstants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OrderSideAnnotationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderSideAnnotation {
    String message() default BrokageConstants.ORDER_SIDE_VIOLATION;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
