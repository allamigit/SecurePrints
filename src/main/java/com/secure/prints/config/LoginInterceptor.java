package com.secure.prints.config;

import com.secure.prints.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoginInterceptor {

    @Before("@annotation(RequiresLogin)")
    public void checkLogin() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        boolean loggedIn = UserService.isUserLoggedIn(request);
        if (!loggedIn) {
            throw new SessionNotActiveException();
        }
    }

}
