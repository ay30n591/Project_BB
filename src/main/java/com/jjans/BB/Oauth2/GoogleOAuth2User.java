package com.jjans.BB.Oauth2;

import java.util.Map;

public class GoogleOAuth2User extends  OAuth2UserInfo{

    public GoogleOAuth2User(Map<String, Object> attributes){
        super(attributes);
    }

    @Override
    public String getOAuth2Id() {
        return (String) attributes.get("sub");
    }
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
    @Override
    public String getName(){
        return (String) attributes.get("nickname");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}
