package com.practice.onlineShop.repositories;

import com.practice.onlineShop.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
