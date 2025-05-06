package com.secure.prints.service;

import com.secure.prints.database.UserRepository;
import com.secure.prints.database.entity.UserEntity;
import com.secure.prints.model.UserLoginResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static UserRepository userRepository;
    private static List<UserEntity> usersList;
    private static UserEntity userEntity;

    /**
     * Constructor for UserService
     * @param userRepository userRepository
     */
    public UserService(UserRepository userRepository) {
        UserService.userRepository = userRepository;
    }

    /**
     * Get all users
     * @return list of all users
     */
    public static List<UserEntity> getAllUsers() {
        usersList = userRepository.findAll();
        return usersList;
    }

    /**
     * Get User Details
     * @param userName userName
     * @return UserEntity
     */
    public static UserEntity getUserByUserName(String userName) {
        List<UserEntity> resultList = usersList.stream()
                .filter(u-> u.getUserName().equals(userName))
                .toList();
        userEntity = resultList != null ?  resultList.get(0) : null;
        return userEntity;
    }

    /**
     * Update User Details
     * @param userEntity userEntity
     */
    public void updateUserDetails(UserEntity userEntity) {
        userRepository.save(userEntity);
        getAllUsers();
    }

    /**
     * User Login
     * @param userName userName
     * @param userPassword userPassword
     * @return UserLoginResponse
     */
    public static UserLoginResponse userLogin(String userName, String userPassword) {
        int responseCode = 409;
        String responseMessage;
        userEntity = getUserByUserName(userName);
        if(userEntity == null) {
            responseMessage = "User not found";
        } else if(!userEntity.getUserStatus()) {
            responseMessage = "User is not active";
        } else if(!userEntity.getUserPassword().equals(userPassword)) {
            responseMessage = "Incorrect password";
        } else {
            responseCode = 200;
            responseMessage = "Logged in successfully";
        }
        return UserLoginResponse.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * User Logout
     * @return UserLoginResponse
     */
    public static UserLoginResponse userLogout() {
        UserLoginResponse userLoginResponse = UserLoginResponse.builder()
                .responseCode(200)
                .responseMessage("Logged out successfully")
                .build();

        userEntity = null;
        return userLoginResponse;
    }

}
