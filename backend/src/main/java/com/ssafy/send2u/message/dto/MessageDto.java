package com.ssafy.send2u.message.dto;

import com.ssafy.send2u.message.entity.Message;
import com.ssafy.send2u.util.AESUtil;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class MessageDto {
    @ApiModelProperty(hidden = true)
    private Long id;

    private Float top;
    private Float left;
    private Float rotate;
    private Long zindex;

    @ApiModelProperty(hidden = true)
    private LocalDateTime createdAt;

    private Long type;

    @ApiModelProperty(hidden = true)
    private String senderId;

    private String receiverId;

    private Long bgcolor;
    private String content;

    @ApiModelProperty(hidden = true)
    private String thumbnailFileUrl;


    @ApiModelProperty(hidden = true)
    private String sourceFileUrl;

    @ApiModelProperty(hidden = true)
    private String senderName;

    @ApiModelProperty(hidden = true)
    private String receiverName;


    public MessageDto(Message message) {
        this.id = message.getId();
        this.top = message.getTopPosition();
        this.left = message.getLeftPosition();
        this.rotate = message.getRotationAngle();
        this.zindex = message.getZIndexValue();
        this.createdAt = message.getCreatedAt();
        this.type = message.getType();
        try {
            this.receiverId = AESUtil.encrypt(message.getReceiver().getUserId());
            this.senderId = AESUtil.encrypt(message.getSender().getUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.bgcolor = message.getBgcolor();
        this.content = message.getContent();
        this.thumbnailFileUrl = message.getThumbnailFileUrl();
        this.sourceFileUrl = message.getSourceFileUrl();
        this.senderName = message.getSender().getUsername();
        this.receiverName = message.getReceiver().getUsername();
    }
}
