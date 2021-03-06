package com.jamesdev.security.jwtv2.service;

import com.jamesdev.security.jwtv2.config.jwt.JwtModel;
import com.jamesdev.security.jwtv2.model.UserOauth;
import com.jamesdev.security.jwtv2.repository.UserOauthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserOauthService {
    private final UserOauthRepository userOauthRepository;
    @Transactional
    public void deleteUserOauth(String username){
        userOauthRepository.deleteByUsername(username);
    }
    public void insertUserOauth(String username,JwtModel jwtModel){
        UserOauth userOauth = UserOauth.builder().refreshToken(jwtModel.getRefreshToken()).username(username).build();
        userOauthRepository.save(userOauth);
    }
    public UserOauth findUserOauthByUsername(String username){
        return userOauthRepository.findByUsername(username);
    }
}
