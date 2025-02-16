package org.lma_it.services;

import org.lma_it.repository.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.lma_it.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepositoryImpl repository;

    public User createUser(User user){
        return repository.create(user);
    }

    public List<User> getAllUsers(){
        return repository.readAll();
    }

    public User updateUser(User user){
        return repository.update(user);
    }

    public User getById(Long id){
        return repository.readById(id);
    }

    public void deleteUser(User user){
        repository.delete(user);
    }
}
