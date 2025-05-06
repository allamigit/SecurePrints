package com.secure.prints.controller;

import com.secure.prints.database.entity.UserEntity;
import com.secure.prints.model.UserLoginResponse;
import com.secure.prints.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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
     * Get all users
     * @return list of all users
     */
    @GetMapping(value = "all-users", produces = MediaType.APPLICATION_JSON_VALUE)
    public static List<UserEntity> getAllUsers() {
        return UserService.getAllUsers();
    }

    /**
     * Get User Details
     * @param userName userName
     * @return UserEntity
     */
    @GetMapping(value = "user", produces = MediaType.APPLICATION_JSON_VALUE)
    public static UserEntity getUserByUserName(@RequestParam(name = "userName") String userName) {
        return UserService.getUserByUserName(userName);
    }

    /**
     * Update User Details
     * @param userEntity userEntity
     */
    @PutMapping(value = "update-user", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateUserDetails(@RequestBody UserEntity userEntity) {
        userService.updateUserDetails(userEntity);
    }

    /**
     * User Login
     * @param userName userName
     * @param userPassword userPassword
     * @return UserLoginResponse
     */
    @PostMapping(value = "login", produces = MediaType.APPLICATION_JSON_VALUE)
    public static UserLoginResponse userLogin(HttpServletResponse response,
                                              @RequestParam(name = "userName") String userName,
                                              @RequestParam(name = "userPassword") String userPassword) {
        UserLoginResponse userLoginResponse = UserService.userLogin(userName, userPassword);
        response.setStatus(userLoginResponse.getResponseCode());
        return userLoginResponse;
    }

    /**
     * User Logout
     * @return UserLoginResponse
     */
    @PostMapping(value = "logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public static UserLoginResponse userLogout(HttpServletResponse response) {
        UserLoginResponse userLoginResponse = UserService.userLogout();
        response.setStatus(userLoginResponse.getResponseCode());
        return userLoginResponse;
    }

}
