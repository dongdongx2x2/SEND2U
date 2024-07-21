package com.ssafy.send2u.message.service;

import com.ssafy.send2u.aws.service.AwsService;
import com.ssafy.send2u.common.error.ErrorCode;
import com.ssafy.send2u.common.error.exception.NoAuthorizationException;
import com.ssafy.send2u.message.dto.MessageDto;
import com.ssafy.send2u.message.entity.Message;
import com.ssafy.send2u.message.repository.MessageRepository;
import com.ssafy.send2u.user.entity.user.User;
import com.ssafy.send2u.user.repository.user.UserRepository;
import com.ssafy.send2u.util.AESUtil;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AwsService awsService;

    @Transactional
    public List<MessageDto> getAllMessages() {
        List<MessageDto> list = messageRepository.findAllWithUsers().stream().map(MessageDto::new).collect(Collectors.toList());
        return list;
    }

    @Transactional
    public List<MessageDto> getUserReceivedMessages(String encryptedUserId) {
        String userId;
        try {
            userId = AESUtil.decrypt(encryptedUserId);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }

        User user = userRepository.findByUserId(userId);

        if (user == null) {
            // 유효하지 않은 사용자 ID인 경우 예외 처리
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }

        List<Message> messages = messageRepository.findByReceiver(user);

        return messages.stream().map(MessageDto::new).collect(Collectors.toList());
    }

    @Transactional
    public MessageDto createMessage(MessageDto messageDto, MultipartFile sourceFile, MultipartFile thumbnailFile)
            throws IOException {

        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

//        User sender = userRepository.findById(senderId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid sender Id:" + senderId));
        User sender = userRepository.findByUserId(principal.getUsername());

        String receiverId;
        try {
            receiverId = AESUtil.decrypt(messageDto.getReceiverId());
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }

        User receiver = userRepository.findByUserId(receiverId);

        String sourceFileURL = null;
        String thumbnailFileUrl = null;

        if (messageDto.getType() == 2 && sourceFile != null && thumbnailFile != null) {
            sourceFileURL = awsService.fileUpload(sourceFile, "image");
            thumbnailFileUrl = awsService.fileUpload(thumbnailFile, "thumbnail");
        } else if (messageDto.getType() == 3 && sourceFile != null && thumbnailFile != null) {
            sourceFileURL = awsService.fileUpload(sourceFile, "video");
            thumbnailFileUrl = awsService.fileUpload(thumbnailFile, "thumbnail");
        }

        Message message = new Message();
        message.setContent(messageDto.getContent());
        message.setTopPosition(messageDto.getTop());
        message.setLeftPosition(messageDto.getLeft());
        message.setRotationAngle(messageDto.getRotate());
        message.setZIndexValue(messageDto.getZindex());
        message.setType(messageDto.getType());
        message.setBgcolor(messageDto.getBgcolor());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSourceFileUrl(sourceFileURL);
        message.setThumbnailFileUrl(thumbnailFileUrl);

        Message savedMessage = messageRepository.save(message);

        return new MessageDto(savedMessage);
    }

    @Transactional
    public MessageDto updateMessage(Long messageId, Float top, Float left, Float rotate, Long zindex) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid message Id: " + messageId));

        if (top != null) {
            message.setTopPosition(top);
        }
        if (left != null) {
            message.setLeftPosition(left);
        }
        if (rotate != null) {
            message.setRotationAngle(rotate);
        }
        if (zindex != null) {
            message.setZIndexValue(zindex);
        }

        Message updatedMessage = messageRepository.save(message);

        return new MessageDto(updatedMessage);
    }

    @Transactional
    public Long deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid message Id: " + messageId));

        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User currentUser = userRepository.findByUserId(principal.getUsername());

        // 본인이 적은 글, 방주인 아니면 오류남
        if (!message.getReceiver().equals(currentUser) && !message.getSender().equals(currentUser)) {
            throw new NoAuthorizationException(ErrorCode.NO_Authorization);
        }

        // 파일이 있는 경우 S3에서도 삭제
        if (message.getSourceFileUrl() != null) {
            awsService.fileDelete(message.getSourceFileUrl());
        }
        if (message.getThumbnailFileUrl() != null) {
            awsService.fileDelete(message.getThumbnailFileUrl());
        }
        messageRepository.deleteById(messageId);
        return messageId;
    }

}