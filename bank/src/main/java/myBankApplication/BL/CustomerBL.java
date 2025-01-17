package myBankApplication.BL;

import myBankApplication.beans.*;
import myBankApplication.services.EmailService;
import myBankApplication.dao.CustomerDAO;

import myBankApplication.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Optional;


@Service
public class CustomerBL {

    @Autowired
    private CustomerDAO customerDAO;

    @Autowired
    private AccountBL accountBL;

    @Autowired
    private BankerBL bankerBL;

    @Autowired
    private UserBL userBL;


    @Autowired
    private EmailService emailService;

    public void checkCustomer(Customer customer) throws CustomerIsNotExistException, CustomerEmailErrorException, CustomerIdErrorException, CustomerLocationErrorException {
        Optional<Customer> existingCustomer = this.customerDAO.findById(customer.getCustomerId());

        if(existingCustomer.isPresent()){
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

    }

    public Customer getCustomer(int id) throws CustomerNotFoundException {
        Optional<Customer> customer = this.customerDAO.findById(id);
        if(customer.isPresent()){
            return customer.get();
        }
        throw new CustomerNotFoundException();
    }


    public void addNewCustomer(Customer customer) throws CustomerEmailErrorException, CustomerLocationErrorException, CustomerIdErrorException, CustomerIsNotExistException, CustomerNotSavedInDataBaseErrorException, UseerNotSavedInDataBaseErrorException, UserUserNameErrorException, UserPasswordErrorException {
        checkCustomer(customer);
        saveCustomerInDataBase(customer);
        User user = new User();
        user.setUserName(customer.getUsername());
        user.setPassword(customer.getPassword());
        //should add create or add
        emailService.sendAccountEmail(customer.getEmail(), "Welcome", "Thank you for creating an account with us.");

        userBL.addNewUser(user);
    }

    public void deleteCustomer(int customerId) throws CustomerIsNotExistException, AccountNotSavedInDataBaseErrorException, AccountNotFoundException, BankerNotFoundException, BankerNotSavedInDataBaseErrorException {
        Optional<Customer> existingCustomer = this.customerDAO.findById(customerId);
        if(!existingCustomer.isPresent()){
            throw new CustomerIsNotExistException();
        }

        List <Account> accountToSuspend = existingCustomer.get().getAccounts();
        for(Account account : accountToSuspend){

            //find bankerBy account id
            Banker responsibleBanker  = bankerBL.getBankerByAccountId(account.getAccountId());
            bankerBL.decrementBankerAccountsByOne(responsibleBanker.getBankerId());
            account.setStatus("Suspended");
            accountBL.saveAccountInDataBase(account);
        }
        existingCustomer.get().setStatus("Suspended");
        this.customerDAO.save(existingCustomer.get());
    }

    public CustomerDAO getCustomerDao() {
        return customerDAO;
    }

    public void setCustomerDoa(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public List<Customer> getAllCustomers() throws CustomerNotFoundException {
        return this.customerDAO.findAll();
    }

    public Customer updateCustomerEmail(int customerId, String newEmail) throws CustomerNotFoundException, CustomerNotSavedInDataBaseErrorException {
        //check the email authintication
        Optional<Customer> customerToUpdate = this.customerDAO.findById(customerId);
        if(customerToUpdate.isPresent()){
            customerToUpdate.get().setEmail(newEmail);
            saveCustomerInDataBase(customerToUpdate.get());
            Optional<Customer> updatedCustomer = this.customerDAO.findById(customerId);
            return updatedCustomer.get();
        }
        else {
            throw new CustomerNotFoundException();

        }
    }

    public Customer updateCustomerLocation(int customerId, String newLocation) throws CustomerNotFoundException, CustomerNotSavedInDataBaseErrorException {
        //check the  authintication
        Optional<Customer> customerToUpdate = this.customerDAO.findById(customerId);
        if (customerToUpdate.isPresent()) {
            customerToUpdate.get().setLocation(newLocation);
            saveCustomerInDataBase(customerToUpdate.get());
            Optional<Customer> updatedCustomer = this.customerDAO.findById(customerId);
            return updatedCustomer.get();
        } else {
            throw new CustomerNotFoundException();
        }
    }

        public boolean saveCustomerInDataBase(Customer customer) throws CustomerNotSavedInDataBaseErrorException {
            try{
                this.customerDAO.save(customer);
                return true;
            }
            catch(Exception e){
                throw new CustomerNotSavedInDataBaseErrorException();
            }
        }


    public void emailVerfiyed(int customerId) throws CustomerNotSavedInDataBaseErrorException, CustomerNotFoundException {
        Optional<Customer> customerToUpdate = this.customerDAO.findById(customerId);
        if (customerToUpdate.isPresent()) {
            customerToUpdate.get().setEmailVerify("EmailVerfiyed");
            saveCustomerInDataBase(customerToUpdate.get());

        } else {
            throw new CustomerNotFoundException();
        }

    }


    public String getCustomerEmail(int customerId) throws CustomerNotFoundException {
        Optional<Customer> customer = this.customerDAO.findById(customerId);
        return customer.get().getEmail();
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void sendBalanceOutOfRangeEmail()  {
        List <Customer> customersList = customerDAO.findAll();
        for (Customer customer : customersList) {
            List<Account> accountList = customer.getAccounts();
            for (Account account : accountList) {
                if(account.getBalance()<0 ){
                    String email = customer.getEmail();
                    emailService.sendAccountEmail(email, "you're balance out of the range", "Sorry, Please check your account.");
                }
            }
        }
    }
}

