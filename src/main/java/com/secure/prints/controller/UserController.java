package com.secure.prints.controller;

import com.secure.prints.database.entity.UserEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping( value = "user")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {

    private final UserService userService;

    /**
     * Constructor for UserController
     * @param userService userService
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Add New User
     * @param userDetails userDetails
     * @return ApiStatus
     */
    @PostMapping(value = "add-user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus addUser(HttpServletResponse response,
                             @RequestBody UserEntity userDetails) {
        ApiStatus apiStatus = userService.addUser(userDetails);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Get all users
     * @return list of all users
     */
    @GetMapping(value = "all-users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Get User Details
     * @param userName userName
     * @return UserEntity
     */
    @GetMapping(value = "user", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserEntity getUserDetails(@RequestParam(name = "userName") String userName) {
        return userService.getUserDetails(userName);
    }

    /**
     * Update User Details
     * @param userDetails userDetails
     */
    @PutMapping(value = "update-user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus updateUserDetails(HttpServletResponse response,
                                       @RequestBody UserEntity userDetails) {
        ApiStatus apiStatus = userService.updateUserDetails(userDetails);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Change User Password
     * @param oldPassword oldPassword
     * @param newPassword newPassword
     * @return ApiStatus
     */
    @PatchMapping(value = "change-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus changeUserPassword(HttpServletResponse response,
                                        @RequestParam(name = "oldPassword") String oldPassword,
                                        @RequestParam(name = "newPassword") String newPassword) {
        ApiStatus apiStatus = userService.changeUserPassword(oldPassword, newPassword);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Reset User Password
     * @param userName userName
     * @param newPassword newPassword
     * @return ApiStatus
     */
    @PatchMapping(value = "reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus resetUserPassword(HttpServletResponse response,
                                        @RequestParam(name = "userName") String userName,
                                        @RequestParam(name = "newPassword") String newPassword) {
        ApiStatus apiStatus = userService.resetUserPassword(userName, newPassword);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * User Login
     * @param userName userName
     * @param userPassword userPassword
     * @return UserLoginResponse
     */
    @PostMapping(value = "login", produces = MediaType.APPLICATION_JSON_VALUE)
    public static ApiStatus userLogin(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(name = "userName") String userName,
                                      @RequestParam(name = "userPassword") String userPassword) {
        ApiStatus apiStatus = UserService.userLogin(request, userName, userPassword);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * User Logout
     * @return UserLoginResponse
     */
    @PostMapping(value = "logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public static ApiStatus userLogout(HttpServletRequest request, HttpServletResponse response) {
        ApiStatus apiStatus = UserService.userLogout(request);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Checks if the user is logged in
     * @return TRUE/FALSE
     */
    @GetMapping(value = "logged-in", produces = MediaType.APPLICATION_JSON_VALUE)
    public static boolean isUserLoggedIn(HttpServletRequest request) {
        return UserService.isUserLoggedIn(request);
    }

}
