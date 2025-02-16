package org.lma_it.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.lma_it.interfaces.UserRepository;

import org.lma_it.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository {


    private final EntityManager entityManager;


    @Autowired
    public UserRepositoryImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    @Override
    public User create(User entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public User readById(Long id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public List<User> readAll() {
        return entityManager.createQuery("FROM User", User.class).getResultList();
    }

    @Override
    public User update(User entity) {
        return entityManager.merge(entity);
    }

    @Override
    public void delete(User entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

}

