package com.github.cunvoas.geoserviceisochrone.repo.extend;

import java.util.Optional;

import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

/**
 * @param <T>
 * @param <ID>
 */
@NoRepositoryBean
public interface ReadOnlyRepository<T, ID> extends Repository<T, ID>, ListPagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T> {

    Optional<T> findById(ID id);
    
    T getReferenceById(ID id);
    
    boolean existsById(ID id);
    
	/**
	 * Returns the number of entities available.
	 * @return the number of entities.
	 */
	long count();


	/**
	 * Returns all instances of the type {@code T} with the given IDs.
	 * <p>
	 * If some or all ids are not found, no entities are returned for these IDs.
	 * <p>
	 * Note that the order of elements in the result is not guaranteed.
	 *
	 * @param ids must not be {@literal null} nor contain any {@literal null} values.
	 * @return guaranteed to be not {@literal null}. The size can be equal or less than the number of given
	 *         {@literal ids}.
	 * @throws IllegalArgumentException in case the given {@link Iterable ids} or one of its items is {@literal null}.
	 */
	Iterable<T> findAllById(Iterable<ID> ids);
}