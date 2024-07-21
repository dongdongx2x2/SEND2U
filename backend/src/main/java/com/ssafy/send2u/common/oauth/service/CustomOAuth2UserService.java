package com.ssafy.send2u.common.oauth.service;

import com.ssafy.send2u.common.oauth.entity.ProviderType;
import com.ssafy.send2u.common.oauth.entity.RoleType;
import com.ssafy.send2u.common.oauth.entity.UserPrincipal;
import com.ssafy.send2u.common.oauth.exception.OAuthProviderMissMatchException;
import com.ssafy.send2u.common.oauth.info.OAuth2UserInfo;
import com.ssafy.send2u.common.oauth.info.OAuth2UserInfoFactory;
import com.ssafy.send2u.message.entity.Message;
import com.ssafy.send2u.message.entity.SecretMessage;
import com.ssafy.send2u.message.repository.MessageRepository;
import com.ssafy.send2u.message.repository.SecretMessageRepository;
import com.ssafy.send2u.user.entity.user.User;
import com.ssafy.send2u.user.repository.user.UserRepository;
import com.ssafy.send2u.util.AESUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final SecretMessageRepository secretMessageRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        try {
            return this.process(userRequest, user);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User user) {
        ProviderType providerType = ProviderType.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase());

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
        User savedUser = userRepository.findByUserId(userInfo.getId());
        userInfo.setImageUrl();

        if (savedUser != null) {
            if (providerType != savedUser.getProviderType()) {
                throw new OAuthProviderMissMatchException(
                        "Looks like you're signed up with " + providerType +
                                " account. Please use your " + savedUser.getProviderType() + " account to login."
                );
            }
            updateUser(savedUser, userInfo);
        } else {
            savedUser = createUser(userInfo, providerType);
        }

        return UserPrincipal.create(savedUser, user.getAttributes());
    }

    private User createUser(OAuth2UserInfo userInfo, ProviderType providerType) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User(
                userInfo.getId(),
                userInfo.getName(),
                userInfo.getImageUrl(),
                providerType,
                RoleType.USER,
                now,
                now
        );
        User user1 = userRepository.saveAndFlush(user);

        Message message = new Message();
        message.setContent("좌측 상단에 물음표를 눌러서 사용방법을 알아보세용!");
        message.setReceiver(user);
        message.setLeftPosition(0.34014f);
        message.setTopPosition(0.716084f);
        message.setRotationAngle(2.04852f);
        message.setZIndexValue(1L);
        message.setType(1L);
        message.setBgcolor(1L);
        message.setSender(userRepository.findByUserId("1111111111"));
        messageRepository.save(message);

        SecretMessage secretMessage = new SecretMessage();

        String encryptedMessage;
        try {
            encryptedMessage = AESUtil.encrypt("고생했어요. 당신의 앞날을 응원합니다.");
        } catch (Exception e) {
            throw new IllegalArgumentException("메시지 내용 암호화에 실패했습니다.");
        }
        secretMessage.setContent(encryptedMessage);
        secretMessage.setReceiver(user);
        secretMessage.setSender(userRepository.findByUserId("1111111111"));
        secretMessage.setBgcolor(1L);
        secretMessage.setType(1L);
        secretMessageRepository.save(secretMessage);

        return user1;
    }

    private User updateUser(User user, OAuth2UserInfo userInfo) {
        if (userInfo.getName() != null && !user.getUsername().equals(userInfo.getName())) {
            user.setUsername(userInfo.getName());
        }

        if (userInfo.getImageUrl() != null && !user.getProfileImageUrl().equals(userInfo.getImageUrl())) {
            user.setProfileImageUrl(userInfo.getImageUrl());
        }

        return user;
    }
}
