package myBankApplication.BL;

import myBankApplication.beans.Customer;
import myBankApplication.dao.CustomerDoa;

import myBankApplication.exceptions.*;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class CustomerBL {


    private CustomerDoa customerDoa;
    public Customer createCustomer(Customer customer) throws CustomerIsNotExistException, EmailErrorException, CustomerEmailErrorException, CustomerIdErrorException, CustomerLocationErrorException {
        //have to take the parameters from customer opject to create new account
        Optional<Customer> existingCustomer= Optional.ofNullable(this.customerDoa.findById(customer.getId()));
        //Create intercase Dao
        //create findById function
        //create in class getId getter
        if(existingCustomer == null){
            throw new CustomerIsNotExistException();
        }
        if(customer.getEmail()==null){
            throw new CustomerEmailErrorException();
        }

        if(customer.getLocation() == null){
            throw new CustomerLocationErrorException();
        }
        if(customer.getCustomerId() == null){
            throw new CustomerIdErrorException();
        }


        return this.customerDoa.save(customer);
    }
    public Customer getCustomer(int id) throws CustomerNotFoundException {
        Optional<Customer>customer = Optional.ofNullable(this.customerDoa.findById(id));
        if(customer.isPresent()){
            return customer.get();
        }
        throw new CustomerNotFoundException();
    }
}