package com.secure.prints.database;

import com.secure.prints.database.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    /**
     * Get User Details
     * @param userId userId
     * @return UserEntity
     */
    UserEntity findByUserId(int userId);

    /**
     * Login User
     * @param userName userName
     * @return UserEntity
     */
    @Query(value = "SELECT u FROM UserEntity u WHERE u.userName = :userName")
    UserEntity userLogin(@Param("userName") String userName);

    /**
     * Change User Password
     * @param userName userName
     * @param userPassword userPassword
     */
    @Modifying
    @Query(value = "UPDATE UserEntity u SET u.userPassword = :userPassword WHERE u.userName = :userName")
    void changeUserPassword(@Param("userName") String userName,
                            @Param("userPassword") String userPassword);

}
