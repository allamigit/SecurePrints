package com.secure.prints.config;

import com.secure.prints.service.UserService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoginInterceptor {

    @Before("@annotation(RequiresLogin)")
    public void checkLogin() {
        if (!UserService.isLoginSessionActive()) {
            throw new SessionNotActiveException();
        }
    }

}
