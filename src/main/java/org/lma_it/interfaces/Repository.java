package org.lma_it.interfaces;

import java.util.List;


public interface Repository<T, L>{
    T create(T entity);
    T readById(Long id);
    List<T> readAll();
    T update(T entity);
    void delete(T entity);

}
