package com.security.jwtpractice.controller;

import com.security.jwtpractice.config.auth.PrincipalDetails;
import com.security.jwtpractice.model.User;
import com.security.jwtpractice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

//@CrossOrigin -> 공부하기
@RestController
@RequiredArgsConstructor
public class RestApiController {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    @GetMapping("/home")
    public String home(){
        return "<h1> home </h1>";
    }
    @GetMapping("/token")
    public String token(){
        return "<h1> token </h1>";
    }

    @PostMapping("/join")
    public String joinProc(User user){
        if(user.getUsername().startsWith("manager_")){
            user.setRoles("ROLE_USER,ROLE_MANAGER");
        }else if (user.getUsername().startsWith("admin_")){
            user.setRoles("ROLE_USER,ROLE_MANAGER,ROLE_ADMIN");
        }else{
            user.setRoles("ROLE_USER");
        }
        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encPassword);
        userRepository.save(user);
        return "회원가입 완료";
    }

    //user, manager, admin 권한만 접근 가능
    @GetMapping("/api/v1/user")
    public String user(HttpServletResponse response, Authentication authentication){
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        System.out.println("authentication : "+principalDetails.getUsername());
        return "user 전용 페이지";
    }

    //manager,admin 권한만 접근 가능
    @GetMapping("/api/v1/manager")
    public String manager(){
        return "manager";
    }

    //admin 권한만 접근 가능
    @GetMapping("/api/v1/admin")
    public String admin(){
        return "admin 전용 페이지";
    }

}
