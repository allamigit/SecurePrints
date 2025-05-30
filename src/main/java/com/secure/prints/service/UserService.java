package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.UserRepository;
import com.secure.prints.database.entity.UserEntity;
import com.secure.prints.model.ApiStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private static UserRepository userRepository;
    private static final String USER_SESSION_KEY = "loggedInUser";

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
    @RequiresLogin
    public ApiStatus addUser(UserEntity userDetails) {
        int responseCode;
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
    @RequiresLogin
    public List<UserEntity> getAllUsers() {
       return userRepository.findAll();
    }

    /**
     * Get User Details
     * @param userId userId
     * @return UserEntity
     */
    @RequiresLogin
    public UserEntity getUserDetails(int userId) {
        return userRepository.findByUserId(userId);
    }

    /**
     * Update User Details
     * @param userDetails userDetails
     */
    @RequiresLogin
    public ApiStatus updateUserDetails(UserEntity userDetails) {
        int responseCode;
        String responseMessage;
        try {
            userRepository.save(userDetails);
            responseCode = 200;
            responseMessage = "User details updated successfully.";
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getMessage();
            if(responseMessage.contains("unique constraint")) {
                responseCode = 409;
                responseMessage = "Duplicate user name.";
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
    public static ApiStatus userLogin(HttpServletRequest request, String userName, String userPassword) {
        int responseCode = 401;
        String responseMessage;
        UserEntity userEntity = userRepository.userLogin(userName, userPassword);
        if(userEntity == null) {
            responseMessage = "User not found.";
        } else if(!userEntity.getUserStatus()) {
            responseMessage = "User is not active.";
        } else if(!userEntity.getUserPassword().equals(userPassword)) {
            responseMessage = "Incorrect password.";
        } else {
            HttpSession session = request.getSession(true);
            session.setAttribute(USER_SESSION_KEY, userEntity.getUserName());
            responseCode = 200;
            responseMessage = userEntity.getUserFullName() + ", logged in as: " + userEntity.getUserName();
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
    public static ApiStatus userLogout(HttpServletRequest request) {
        int responseCode = 409;
        String responseMessage;
        HttpSession session = request.getSession(false);
        if(session == null) {
            responseMessage = "There is no active login session.";
        } else {
            responseCode = 200;
            responseMessage = session.getAttribute(USER_SESSION_KEY) + ", logged out successfully.";
            session.invalidate();
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Checks if the user is logged in
     * @return TRUE/FALSE
     */
    public static boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(USER_SESSION_KEY) != null;
    }

}
