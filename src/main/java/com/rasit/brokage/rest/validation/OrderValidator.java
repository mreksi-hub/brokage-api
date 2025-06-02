package com.rasit.brokage.rest.validation;

import com.rasit.brokage.core.data.OrderDao;
import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.utility.ErrorMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


/**
 * Validator for order details,
 */
@Slf4j
@Component
@Validated
public class OrderValidator {
    private final OrderDao orderDao;

    @Autowired
    public OrderValidator(final OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    /**
     * Validates that an order with the given ID exists.
     * If a customer ID is provided, it further validates that the order belongs to that customer.
     * Throws a CustomException if the order does not exist or does not belong to the specified customer.
     *
     * @param orderId    The ID of the order to check.
     * @param customerId The ID of the customer. Can be null for admin checks (i.e., no customer ownership check needed).
     * @throws CustomException if the order is not found or does not belong to the customer.
     */
    public void validateOrderExistenceAndOwnership(String orderId, String customerId) throws CustomException {
        if (customerId == null) {
            if (orderDao.isOrderExistsWithOrderId(orderId)) {
                log.debug("Order exists with the orderId : {}", orderId);
                return;
            }
        } else {
            if (orderDao.isOrderExistsWithOrderIdAndCustomerId(orderId, customerId)) {
                log.debug("Order exists with the orderId : {} and customerId {}", orderId, customerId);
                return;
            }
        }
        throw new CustomException(ErrorMessageType.ORDER_NOT_FOUND,
                new String[]{orderId},
                HttpStatus.NOT_FOUND);
    }
}
