package com.rasit.brokage.rest.validation;

import com.rasit.brokage.rest.resource.order.OrderRestRequestModel;
import com.rasit.brokage.rest.validation.annotation.OrderSideAnnotation;
import com.rasit.brokage.utility.SideType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrderSideAnnotationValidator implements ConstraintValidator<OrderSideAnnotation, OrderRestRequestModel> {

    @Override
    public boolean isValid(OrderRestRequestModel orderRestRequestModel, ConstraintValidatorContext constraintValidatorContext) {
        String orderSide = orderRestRequestModel.getOrderSide().getValue();
        return !(SideType.findByValue(orderSide) == null);
    }
}
