package com.secure.prints.controller;

import com.secure.prints.database.entity.UserEntity;
import com.secure.prints.model.ApiStatus;
import com.secure.prints.model.ChangePassword;
import com.secure.prints.model.ResetPassword;
import com.secure.prints.model.UserLogin;
import com.secure.prints.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping( value = "user")
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
     * @param changePassword changePassword
     * @return ApiStatus
     */
    @PatchMapping(value = "change-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus changeUserPassword(HttpServletResponse response,
                                        @RequestBody ChangePassword changePassword) {
        ApiStatus apiStatus = userService.changeUserPassword(changePassword);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * Reset User Password
     * @param resetPassword resetPassword
     * @return ApiStatus
     */
    @PatchMapping(value = "reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiStatus resetUserPassword(HttpServletResponse response,
                                        @RequestBody ResetPassword resetPassword) {
        ApiStatus apiStatus = userService.resetUserPassword(resetPassword);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * User Login
     * @param userLogin userLogin
     * @return  ApiStatus
     */
    @PostMapping(value = "login", produces = MediaType.APPLICATION_JSON_VALUE)
    public static ApiStatus userLogin(HttpServletRequest request, HttpServletResponse response,
                                        @RequestBody UserLogin userLogin) {
        ApiStatus apiStatus = UserService.userLogin(request, userLogin);
        response.setStatus(apiStatus.getResponseCode());
        return apiStatus;
    }

    /**
     * User Logout
     * @return ApiStatus
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
