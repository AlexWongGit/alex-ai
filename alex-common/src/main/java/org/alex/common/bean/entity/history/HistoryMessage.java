package org.alex.common.bean.entity.history;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Date;
import java.util.Collections;
import java.util.Map;

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

    @TableField("question")
    private String question;

    @TableField("answer")
    private String answer;

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
        if (MessageType.ASSISTANT == type) {
            this.messageType = "assistant";
        }
        if (MessageType.SYSTEM == type) {
            this.messageType = "system";
        }
        if (MessageType.TOOL == type) {
            this.messageType = "tool";
        }
        throw new RuntimeException("message type is null");
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
