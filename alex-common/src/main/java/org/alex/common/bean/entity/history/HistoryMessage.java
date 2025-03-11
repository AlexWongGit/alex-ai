package org.alex.common.bean.entity.history;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Date;

/**
 * TODO <br>
 *
 * @Author wangzf
 * @Date 2025/3/10
 */
@TableName("history_message")
public class HistoryMessage {

    @TableId(type = IdType.ASSIGN_ID)
    @TableField("id")
    private String id;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("message_id")
    private String messageId;

    @TableField("text")
    private String text;

    @TableField("message_order")
    private Long messageOrder;
    @TableField("metadata")
    private String metadata;

    @TableField("message_type")
    private String messageType;

    @TableField("ask_time")
    private Date askTime;

    @TableField("answer_time")
    private Date answerTime;

    @TableField("create_time")
    private Date createTime;



    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public void setMessageType(MessageType type) {

        if (MessageType.USER == type) {
            this.messageType = "user";
        }
        else if (MessageType.ASSISTANT == type) {
            this.messageType = "assistant";
        }
        else if (MessageType.SYSTEM == type) {
            this.messageType = "system";
        } else if (MessageType.TOOL == type) {
            this.messageType = "tool";
        } else {
            throw new RuntimeException("message type is null");
        }
    }

    public MessageType getMessageType() {

        if ("user".equalsIgnoreCase(messageType)) {
            return MessageType.USER;
        }
        if ("assistant".equalsIgnoreCase(messageType)) {
            return MessageType.ASSISTANT;
        }
        if ("system".equalsIgnoreCase(messageType)) {
            return MessageType.SYSTEM;
        }
        if ("tool".equalsIgnoreCase(messageType)) {
            return MessageType.TOOL;
        }
        return null;
    }


    public Date getAskTime() {
        return askTime;
    }

    public void setAskTime(Date askTime) {
        this.askTime = askTime;
    }

    public Date getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(Date answerTime) {
        this.answerTime = answerTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getMessageOrder() {
        return messageOrder;
    }

    public void setMessageOrder(Long messageOrder) {
        this.messageOrder = messageOrder;
    }
}
