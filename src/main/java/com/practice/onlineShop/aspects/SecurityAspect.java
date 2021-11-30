package com.practice.onlineShop.aspects;

import com.practice.onlineShop.entities.User;
import com.practice.onlineShop.enums.Roles;
import com.practice.onlineShop.exceptions.InvalidCustomerIdException;
import com.practice.onlineShop.exceptions.InvalidOperationException;
import com.practice.onlineShop.repositories.UserRepository;
import com.practice.onlineShop.vos.OrderVO;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {
    private final UserRepository userRepository;

    @Pointcut("execution(* com.practice.onlineShop.services.ProductService.addProduct(..))")
    public void addProduct() {}

    @Pointcut("execution(* com.practice.onlineShop.services.ProductService.updateProduct(..))")
    public void updateProduct() {}

    @Pointcut("execution(* com.practice.onlineShop.services.ProductService.deleteProduct(..))")
    public void deleteProduct() {}

    @Pointcut("execution(* com.practice.onlineShop.services.ProductService.addStock(..))")
    public void addStock() {}

    @Pointcut("execution(* com.practice.onlineShop.services.OrderService.addOrder(..))")
    public void addOrderPointcut() {}

    @Pointcut("execution(* com.practice.onlineShop.services.OrderService.deliver(..))")
    public void deliverPointcut() {}

    @Pointcut("execution(* com.practice.onlineShop.services.OrderService.cancelOrder(..))")
    public void cancelOrderPointcut() {}

    @Pointcut("execution(* com.practice.onlineShop.services.OrderService.returnOrder(..))")
    public void returnOrderPointcut() {}

    @Before("com.practice.onlineShop.aspects.SecurityAspect.addProduct()")
    public void checkSecurityBeforeAddingProduct(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<User> userOptional = userRepository.findById(customerId);

        if (!userOptional.isPresent()) {
            throw new InvalidCustomerIdException();
        }
        User user = userOptional.get();

        if (userIsNotAllowedToAddProduct(user.getRoles())) {
            throw new InvalidOperationException();
        }

        System.out.println(customerId);
    }

    @Before("com.practice.onlineShop.aspects.SecurityAspect.updateProduct()")
    public void checkSecurityBeforeUpdatingProduct(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<User> userOptional = userRepository.findById(customerId);

        if (!userOptional.isPresent()) {
            throw new InvalidCustomerIdException();
        }
        User user = userOptional.get();

        if (userIsNotAllowedToUpdateProduct(user.getRoles())) {
            throw new InvalidOperationException();
        }

        System.out.println(customerId);
    }

    @Before("com.practice.onlineShop.aspects.SecurityAspect.addStock()")
    public void checkSecurityBeforeAddingStock(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[2];
        Optional<User> userOptional = userRepository.findById(customerId);

        if (!userOptional.isPresent()) {
            throw new InvalidCustomerIdException();
        }
        User user = userOptional.get();

        if (userIsNotAllowedToAddStock(user.getRoles())) {
            throw new InvalidOperationException();
        }

        System.out.println(customerId);
    }

    @Before("com.practice.onlineShop.aspects.SecurityAspect.deleteProduct()")
    public void checkSecurityBeforeDeletingAProduct(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<User> userOptional = userRepository.findById(customerId);

        if (!userOptional.isPresent()) {
            throw new InvalidCustomerIdException();
        }
        User user = userOptional.get();

        if (userIsNotAllowedToDeleteProduct(user.getRoles())) {
            throw new InvalidOperationException();
        }

        System.out.println(customerId);
    }

    @Before("com.practice.onlineShop.aspects.SecurityAspect.addOrderPointcut()")
    public void checkSecurityBeforeAddingAnOrder(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        OrderVO orderVO = (OrderVO) joinPoint.getArgs()[0];
        
        if (orderVO.getUserId() == null){
            throw new InvalidCustomerIdException();
        }
        Optional<User> userOptional = userRepository.findById(orderVO.getUserId().longValue());
        
        
        if (!userOptional.isPresent()) {
            throw new InvalidCustomerIdException();
        }
        User user = userOptional.get();

        if (userIsNotAllowedToAddAnOrder(user.getRoles())) {
            throw new InvalidOperationException();
        }
    }

    @Before("com.practice.onlineShop.aspects.SecurityAspect.deliverPointcut()")
    public void checkSecurityBeforeDeliver(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<User> userOptional = userRepository.findById(customerId.longValue());

        if (!userOptional.isPresent()) {
            throw new InvalidCustomerIdException();
        }
        User user = userOptional.get();

        if (userIsnotAllowedToDeliver(user.getRoles())) {
            throw new InvalidOperationException();
        }

        System.out.println(customerId);
    }

    @Before("com.practice.onlineShop.aspects.SecurityAspect.cancelOrderPointcut()")
    public void checkSecurityBeforeCancellingOrder(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<User> userOptional = userRepository.findById(customerId.longValue());

        if (!userOptional.isPresent()) {
            throw new InvalidCustomerIdException();
        }
        User user = userOptional.get();

        if (userIsNotAllowedToCancel(user.getRoles())) {
            throw new InvalidOperationException();
        }

        System.out.println(customerId);
    }

    @Before("com.practice.onlineShop.aspects.SecurityAspect.returnOrderPointcut()")
    public void checkSecurityBeforeReturningOrder(JoinPoint joinPoint) throws InvalidCustomerIdException, InvalidOperationException {
        Long customerId = (Long) joinPoint.getArgs()[1];
        Optional<User> userOptional = userRepository.findById(customerId);

        if (!userOptional.isPresent()) {
            throw new InvalidCustomerIdException();
        }
        User user = userOptional.get();

        if (userIsNotAllowedToReturnOrder(user.getRoles())) {
            throw new InvalidOperationException();
        }

        System.out.println(customerId);
    }

    private boolean userIsNotAllowedToAddStock(Collection<Roles> roles) {
        return !roles.contains(Roles.ADMIN);
    }

    private boolean userIsNotAllowedToReturnOrder(Collection<Roles> roles) {
        return !roles.contains(Roles.CLIENT);
    }

    private boolean userIsNotAllowedToCancel(Collection<Roles> roles) {
        return !roles.contains(Roles.CLIENT);
    }

    private boolean userIsnotAllowedToDeliver(Collection<Roles> roles) {
        return !roles.contains(Roles.EXPEDITOR);
    }

    private boolean userIsNotAllowedToAddAnOrder(Collection<Roles> roles) {
        return !roles.contains(Roles.CLIENT);
    }

    private boolean userIsNotAllowedToAddProduct(Collection<Roles> roles) {
        return !roles.contains(Roles.ADMIN);
    }

    private boolean userIsNotAllowedToDeleteProduct(Collection<Roles> roles) {
        return !roles.contains(Roles.ADMIN);
    }

    private boolean userIsNotAllowedToUpdateProduct(Collection<Roles> roles) {
        return !roles.contains(Roles.ADMIN) && !roles.contains(Roles.EDITOR);
    }
}





































