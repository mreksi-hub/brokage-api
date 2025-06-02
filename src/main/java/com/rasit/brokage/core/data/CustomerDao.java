package com.rasit.brokage.core.data;

import com.rasit.brokage.core.data.entity.CustomerEntity;

import java.util.Optional;

public interface CustomerDao {
    Optional<CustomerEntity> findByUsername(String username);

    CustomerEntity save(CustomerEntity customerEntity);

    void deleteAll();
}
