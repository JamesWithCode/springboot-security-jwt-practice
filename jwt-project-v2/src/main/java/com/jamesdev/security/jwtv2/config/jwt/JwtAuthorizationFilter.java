package com.jamesdev.security.jwtv2.config.jwt;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.jamesdev.security.jwtv2.config.auth.PrincipalDetailsService;
import com.jamesdev.security.jwtv2.model.UserOauth;
import com.jamesdev.security.jwtv2.service.UserOauthService;
import com.jamesdev.security.jwtv2.service.UserService;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.ObjectUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {


    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserService userService,JwtService jwtService, UserOauthService userOauthService,PrincipalDetailsService principalDetailsService) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.userService=userService;
        this.userOauthService = userOauthService;
        this.principalDetailsService=principalDetailsService;
    }

    private final JwtService jwtService;
    private final UserOauthService userOauthService;
    private final PrincipalDetailsService principalDetailsService;
    private final UserService userService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("=====================================");
        System.out.println("JwtAuthorizationFilter ??????");
        /*    (1)   */
        String accessToken = jwtService.resolveCookie(request);
        String refreshToken = null;
        String username=null;
        System.out.println("accessToken : "+accessToken);
        /*    (2)   */
        //access ?????? ??????
        try{
            if(StringUtils.isNotBlank(accessToken) && jwtService.validateToken(accessToken)){
                //????????? ??????????????? ?????? ????????? ?????? ????????? ?????????
                Authentication auth = this.getAuthentication(accessToken);
                System.out.println("?????? ??????");
                SecurityContextHolder.getContext().setAuthentication(auth);
            }//access ??????????????? refresh ?????? ????????????
            //TODO : ???????????? ?????? ???????????? ???????????? & ACCESS ?????? ?????? ???????????????
        /*    (3)   */
        }catch(TokenExpiredException e){
            System.out.println("access ?????? ?????????");
            username = jwtService.getClaimFromExpiredToken(accessToken,"username"); //?????????  ???????????? ???????????? ????????? ??????
            System.out.println("username : "+username);
            UserOauth userOauth = userOauthService.findUserOauthByUsername(username);
            if(!ObjectUtils.isEmpty(userOauth)){
                refreshToken =userOauth.getRefreshToken(); //db?????? ?????????????????? ???????????? ?????? ????????????
                System.out.println("refreshToken : "+refreshToken);
            }
        }catch(Exception e){
            SecurityContextHolder.clearContext();
            System.out.println("JwtAuthorizationFilter internal error "+ e.getMessage());
            return;
        }
        /*    (4)   */
        //refresh ???????????? access ?????? ??????
        if(StringUtils.isNotBlank(refreshToken)){
            try{
                try{
                    if(jwtService.validateToken(refreshToken)){
                        Authentication auth = this.getAuthentication(refreshToken);
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        //????????? accessToken ??????
                        String newAccessToken = jwtService.createToken(username).getAccessToken();
                        //????????? ?????????
                        jwtService.createCookie(response, newAccessToken);
                    }
                }catch(TokenExpiredException e){
                    System.out.println("JWT token expired : "+e.getMessage());
                }
            }catch(Exception e){
                SecurityContextHolder.clearContext();
                System.out.println("JwtAuthorizationFilter internal error "+ e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request,response);
    }

    /*    (5)   */
    public Authentication getAuthentication(String token){
        String username=jwtService.getClaim(token,"username");
        UserDetails userDetails = principalDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null,userDetails.getAuthorities());
    }

}
