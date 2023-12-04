package com.jjans.BB.Service;

import com.jjans.BB.Entity.Users;
import com.jjans.BB.Enum.AuthProvider;
import com.jjans.BB.Enum.Authority;
import com.jjans.BB.Oauth2.OAuth2UserInfo;
import com.jjans.BB.Oauth2.OAuth2UserInfoFactory;
import com.jjans.BB.Oauth2.UserPrincipal;
import com.jjans.BB.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.logging.Logger;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService  implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UsersRepository usersRepository;
    private Logger logger;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException{
        OAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(oAuth2UserRequest);
        return processOAuth2User(oAuth2UserRequest, oAuth2User);
    }
    protected OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        //OAuth2 로그인 플랫폼 구분
        AuthProvider authProvider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(authProvider, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        Users user = usersRepository.findByEmail(oAuth2UserInfo.getEmail()).orElse(null);
        //이미 가입된 경우
        if (user != null) {
            if (!user.getAuthProvider().equals(authProvider)) {
                throw new RuntimeException("Email already signed up.");
            }
            user = updateUser(user, oAuth2UserInfo);
        }
        //가입되지 않은 경우
        else {
            user = registerUser(authProvider, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2UserInfo.getAttributes());
    }

    private Users registerUser(AuthProvider authProvider, OAuth2UserInfo oAuth2UserInfo) {
        logger.info(oAuth2UserInfo.getEmail());
        Users users = Users.builder()
                .email(oAuth2UserInfo.getEmail())
                .nickName(oAuth2UserInfo.getName())
                .oauth2Id(oAuth2UserInfo.getOAuth2Id())
                .authProvider(authProvider)
//                .roles(Authority.ROLE_USER)
                .build();

        return usersRepository.save(users);
    }

    private Users updateUser(Users user, OAuth2UserInfo oAuth2UserInfo) {

        return usersRepository.save(user.update(oAuth2UserInfo));
    }
}