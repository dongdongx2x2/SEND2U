package com.ssafy.send2u.message.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

import com.ssafy.send2u.common.oauth.entity.ProviderType;
import com.ssafy.send2u.common.oauth.entity.RoleType;
import com.ssafy.send2u.message.entity.Message;
import com.ssafy.send2u.message.repository.MessageRepository;
import com.ssafy.send2u.user.entity.user.User;
import com.ssafy.send2u.user.repository.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "cloud.aws.s3.bucket=",
        "cloud.aws.credentials.access-key=",
        "cloud.aws.credentials.secret-key=",
        "JWT_SECRET_KEY=MyTestJWTSecretKeyWhichIsAtLeast256BitsLong",
        "AUTHENTICATION_TOKEN_SECRET=MyTestAuthTokenSecret",
        "KAKAO_CLIENT_ID_KEY=dummy",
        "KAKAO_CLIENT_SECRET_KEY=dummy",
        "S3_ACCESS_KEY=",
        "S3_SECRET_KEY=",
        "KAKAO_API_ADMIN_KEY=testAdminKey",
        "spring.security.oauth2.client.registration.kakao.client-id=dummy",
        "spring.security.oauth2.client.registration.kakao.client-secret=dummy",
        "spring.security.oauth2.client.provider.kakao.authorization-uri=https://example.com",
        "spring.security.oauth2.client.provider.kakao.token-uri=https://example.com",
        "spring.security.oauth2.client.provider.kakao.user-info-uri=https://example.com",
        "spring.security.oauth2.client.provider.kakao.user-name-attribute=id"
})
@ActiveProfiles("test")
public class MessageServiceTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFetchJoinPerformance() {
        // 데이터 초기화
        User user1 = new User();
        user1.setUserId("user1_id");  // ID 수동 할당
        user1.setUsername("User1");
        user1.setProfileImageUrl("profile1.jpg");
        user1.setProviderType(ProviderType.KAKAO);
        user1.setRoleType(RoleType.USER);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setModifiedAt(LocalDateTime.now());
        userRepository.save(user1);

        User user2 = new User();
        user2.setUserId("user2_id");  // ID 수동 할당
        user2.setUsername("User2");
        user2.setProfileImageUrl("profile2.jpg");
        user2.setProviderType(ProviderType.KAKAO);
        user2.setRoleType(RoleType.USER);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setModifiedAt(LocalDateTime.now());
        userRepository.save(user2);

        for (int i = 0; i < 50000; i++) {
            Message message = new Message();
            message.setContent("Message " + i);
            message.setSender(user1);
            message.setReceiver(user2);
            message.setTopPosition(0.0f);
            message.setLeftPosition(0.0f);
            message.setRotationAngle(0.0f);
            message.setZIndexValue(0L);
            message.setType(1L);
            messageRepository.save(message);
        }

        long startTime = System.currentTimeMillis();
        List<Message> messages = messageRepository.findAll();
        long endTime = System.currentTimeMillis();
        System.out.println("findAll시간++++++++++++++++" + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        List<Message> messagesWithFetchJoin = messageRepository.findAllWithUsers();
        endTime = System.currentTimeMillis();
        System.out.println("findAllWithUsers시간(fetchjoin)+++++++++++++++" + (endTime - startTime) + " ms");

        assertNotNull(messages);
        assertNotNull(messagesWithFetchJoin);
    }

}
