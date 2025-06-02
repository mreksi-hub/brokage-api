package com.rasit.brokage.rest.validation.annotation;

import com.rasit.brokage.rest.validation.StartDateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StartDateValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StartDateAnnotation {
    String message() default "Invalid startDate format. Expected yyyy-MM-dd'T'HH:mm:ssZ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
