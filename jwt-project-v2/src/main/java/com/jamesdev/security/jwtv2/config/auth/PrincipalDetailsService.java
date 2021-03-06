package com.jamesdev.security.jwtv2.config.auth;

import com.jamesdev.security.jwtv2.model.User;
import com.jamesdev.security.jwtv2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PrincipalDetailsService  implements UserDetailsService {
    private final UserService userService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("================================");
        System.out.println("username : "+username);
        User user=userService.findUserByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("ID : "+ username + " not found.");
        }
        System.out.println("User Entity : "+ user.toString());
        return new  PrincipalDetails(user);
    }
}
