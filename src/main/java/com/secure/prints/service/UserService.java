package com.secure.prints.service;

import com.secure.prints.database.UserRepository;
import com.secure.prints.database.entity.UserEntity;
import com.secure.prints.model.ApiStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
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
     * Add New User
     * @param userDetails userDetails
     * @return ApiStatus
     */
    public ApiStatus addUser(UserEntity userDetails) {
        int responseCode = 409;
        String responseMessage;
        try {
            userDetails.setUserId(userRepository.getNextUserId());
            userRepository.save(userDetails);
            getAllUsers();
            responseCode = 200;
            responseMessage = "User added successfully.";
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getMessage();
            if(responseMessage.contains("unique constraint")) {
                responseMessage = "Duplicate user name.";
            }
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
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
                .filter(u -> u.getUserName().equals(userName))
                .toList();
        userEntity = !resultList.isEmpty() ? resultList.get(0) : null;
        return userEntity;
    }

    /**
     * Update User Details
     * @param userDetails userDetails
     */
    public ApiStatus updateUserDetails(UserEntity userDetails) {
        int responseCode = 409;
        String responseMessage;
        List<UserEntity> resultList = usersList.stream()
                .filter(u -> u.getUserId().equals(userDetails.getUserId()))
                .toList();
        if(resultList.get(0).equals(userDetails)) {
            responseMessage = "There is no change to update.";
        } else {
            try {
                userRepository.save(userDetails);
                getAllUsers();
                responseCode = 200;
                responseMessage = "User details updated successfully.";
            } catch (Exception e) {
                responseCode = 400;
                responseMessage = e.getMessage();
                if(responseMessage.contains("unique constraint")) {
                    responseMessage = "Duplicate user name.";
                }
            }
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * User Login
     * @param userName userName
     * @param userPassword userPassword
     * @return ApiStatus
     */
    public static ApiStatus userLogin(String userName, String userPassword) {
        int responseCode = 401;
        String responseMessage;
        userEntity = getUserByUserName(userName);
        if(userEntity == null) {
            responseMessage = "User not found.";
        } else if(!userEntity.getUserStatus()) {
            responseMessage = "User is not active.";
        } else if(!userEntity.getUserPassword().equals(userPassword)) {
            responseMessage = "Incorrect password.";
        } else {
            responseCode = 200;
            responseMessage = "Logged in successfully.";
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * User Logout
     * @return ApiStatus
     */
    public static ApiStatus userLogout() {
        int responseCode = 409;
        String responseMessage;
        if(userEntity == null) {
            responseMessage = "There is no active login session.";
        } else {
            responseCode = 200;
            responseMessage = "Logged out successfully.";
            userEntity = null;
        }

        ApiStatus apiStatus = ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
        return apiStatus;
    }

    /**
     * Checks if there is an active login session
     * @return TRUE/FALSE
     */
    public static boolean isLoginSessionActive() {
        return userEntity != null;
    }

}
