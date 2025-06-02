package com.rasit.brokage.core.data;

import com.rasit.brokage.core.data.entity.CustomerEntity;
import com.rasit.brokage.core.data.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
public class CustomerDaoImpl implements CustomerDao {
    private final CustomerRepository customerRepository;

    public CustomerDaoImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Optional<CustomerEntity> findByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    @Override
    public CustomerEntity save(CustomerEntity customerEntity) {
        return customerRepository.save(customerEntity);
    }

    @Override
    public void deleteAll() {
        customerRepository.deleteAll();
    }
}
