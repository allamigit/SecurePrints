package com.secure.prints.database;

import com.secure.prints.database.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    /**
     * Get next value of user sequence
     * @return nextUserId
     */
    @Query(value = "SELECT nextval('usr_info_seq')")
    int getNextUserId();

}
