package com.secure.prints.service;

import com.secure.prints.config.RequiresLogin;
import com.secure.prints.database.UserRepository;
import com.secure.prints.database.entity.UserEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.util.NameFormatUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        String userName = userDetails.getUserName().trim().toLowerCase();
        UserEntity user = this.getUserDetails(userName);
        try {
            if(user != null) {
                throw new DataIntegrityViolationException("User already exists");
            }
            userDetails.setUserName(userName);
            userDetails.setUserPassword(encryptPassword(userDetails.getUserPassword()));
            userDetails.setUserFullName(NameFormatUtil.formatName(userDetails.getUserFullName()));
            userRepository.save(userDetails);
            responseCode = 201;
            responseMessage = "User added successfully.";
        } catch (DataIntegrityViolationException e) {
            responseCode = 409;
            responseMessage = "Duplicate user name.";
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
     * @param userName userName
     * @return UserEntity
     */
    public UserEntity getUserDetails(String userName) {
        return userRepository.findByUserName(userName.toLowerCase());
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
            userDetails.setUserFullName(NameFormatUtil.formatName(userDetails.getUserFullName()));
            userRepository.save(userDetails);
            responseCode = 200;
            responseMessage = "User details updated successfully.";
        } catch (Exception e) {
            responseCode = 400;
            responseMessage = e.getMessage();
        }

        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Change User Password
     * @param oldPassword oldPassword
     * @param newPassword newPassword
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus changeUserPassword(String oldPassword, String newPassword) {
        int responseCode = 409;
        String responseMessage;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = request.getSession(false);
        String userName = session.getAttribute(USER_SESSION_KEY).toString();
        if(validatePassword(newPassword, oldPassword)) {
            responseMessage = "The new password is the same of current password.";
        } else {
            userRepository.changeUserPassword(userName, encryptPassword(newPassword));
            responseCode = 200;
            responseMessage = "Success password changed.";
        }
        return ApiStatus.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * Reset User Password
     * @param userName userName
     * @param newPassword newPassword
     * @return ApiStatus
     */
    @RequiresLogin
    public ApiStatus resetUserPassword(String userName, String newPassword) {
        int responseCode = 409;
        String responseMessage;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = request.getSession(false);
        String currentUserName = session.getAttribute(USER_SESSION_KEY).toString();
        if(currentUserName.equals(userName)) {
            responseMessage = "Reset password is only allowed for other users.";
        } else {
            userRepository.changeUserPassword(userName, encryptPassword(newPassword));
            responseCode = 200;
            responseMessage = "Success reset password to [user1234].";
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
        UserEntity userEntity = userRepository.findByUserName(userName.toLowerCase());
        if(userEntity == null) {
            responseMessage = "User not found.";
        } else if(!userEntity.getUserStatus()) {
            responseMessage = "User is not active.";
        } else if(!validatePassword(userPassword, userEntity.getUserPassword())) {
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

    /**
     * Password Encryptor
     * @param rawPassword rawPassword
     * @return Encrypted Password
     */
    private static String encryptPassword(String rawPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Validate entered password with DB password
     * @param rawPassword rawPassword
     * @param encryptedPassword encryptedPassword
     * @return TRUE = Match / FALSE = Mismatch
     */
    private static boolean validatePassword(String rawPassword, String encryptedPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encryptedPassword);
    }

}
