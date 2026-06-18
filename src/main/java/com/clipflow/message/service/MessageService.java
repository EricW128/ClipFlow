package com.clipflow.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clipflow.common.PageResponse;
import com.clipflow.common.exception.BusinessException;
import com.clipflow.message.dto.ConversationResponse;
import com.clipflow.message.dto.MessageResponse;
import com.clipflow.message.entity.Conversation;
import com.clipflow.message.entity.PrivateMessage;
import com.clipflow.message.mapper.ConversationMapper;
import com.clipflow.message.mapper.PrivateMessageMapper;
import com.clipflow.user.entity.User;
import com.clipflow.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final ConversationMapper conversationMapper;
    private final PrivateMessageMapper privateMessageMapper;
    private final UserMapper userMapper;

    public MessageService(
            ConversationMapper conversationMapper,
            PrivateMessageMapper privateMessageMapper,
            UserMapper userMapper) {
        this.conversationMapper = conversationMapper;
        this.privateMessageMapper = privateMessageMapper;
        this.userMapper = userMapper;
    }

    private Long getUser1Id(Long senderId, Long receiverId) {
        return Math.min(senderId, receiverId);
    }

    private Long getUser2Id(Long senderId, Long receiverId) {
        return Math.max(senderId, receiverId);
    }

    private void validateReceiver(
            Long senderId,
            Long receiverId) {

        if (senderId.equals(receiverId)) {
            throw new BusinessException(
                    26001,
                    "不能给自己发送私信"
            );
        }

        if (userMapper.selectById(receiverId) == null) {
            throw new BusinessException(
                    26002,
                    "接收者不存在"
            );
        }
    }

    private Conversation findOrCreateConversation(
            Long senderId,
            Long receiverId) {

        Long user1Id = getUser1Id(senderId, receiverId);
        Long user2Id = getUser2Id(senderId, receiverId);

        Conversation conversation =
                conversationMapper.selectOne(
                        new LambdaQueryWrapper<Conversation>()
                                .eq(Conversation::getUser1Id, user1Id)
                                .eq(Conversation::getUser2Id, user2Id)
                );

        if (conversation != null) {
            return conversation;
        }

        conversation = new Conversation();
        conversation.setUser1Id(user1Id);
        conversation.setUser2Id(user2Id);

        conversationMapper.insert(conversation);

        return conversation;
    }

    @Transactional
    public Long sendMessage(
            Long senderId,
            Long receiverId,
            String content) {

        validateReceiver(senderId, receiverId);

        Conversation conversation =
                findOrCreateConversation(senderId, receiverId);

        LocalDateTime now = LocalDateTime.now();

        PrivateMessage message = new PrivateMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setContent(content);
        message.setCreatedAt(now);

        privateMessageMapper.insert(message);

        conversation.setLastMessageContent(content);
        conversation.setLastMessageAt(now);
        conversationMapper.updateById(conversation);

        return message.getId();
    }

    public List<ConversationResponse> getUserConversations(
            Long userId) {

        List<Conversation> conversations =
                conversationMapper.selectList(
                        new LambdaQueryWrapper<Conversation>()
                                .and(wrapper -> wrapper
                                        .eq(Conversation::getUser1Id, userId)
                                        .or()
                                        .eq(Conversation::getUser2Id, userId)
                                )
                                .orderByDesc(Conversation::getLastMessageAt)
                );

        if (conversations.isEmpty()) {
            return List.of();
        }

        List<Long> otherUserIds = conversations.stream()
                .map(conversation ->
                        getOtherUserId(conversation, userId))
                .distinct()
                .toList();

        Map<Long, User> userMap =
                userMapper.selectBatchIds(otherUserIds)
                        .stream()
                        .collect(Collectors.toMap(
                                User::getId,
                                Function.identity()
                        ));

        return conversations.stream()
                .map(conversation -> {
                    Long otherUserId =
                            getOtherUserId(conversation, userId);

                    User otherUser = userMap.get(otherUserId);

                    return new ConversationResponse(
                            conversation.getId(),
                            otherUserId,
                            otherUser.getNickname(),
                            otherUser.getAvatarUrl(),
                            conversation.getLastMessageContent(),
                            conversation.getLastMessageAt()
                    );
                })
                .toList();
    }

    private Long getOtherUserId(
            Conversation conversation,
            Long currentUserId) {

        if (conversation.getUser1Id().equals(currentUserId)) {
            return conversation.getUser2Id();
        }

        return conversation.getUser1Id();
    }

    private Conversation getAccessibleConversation(
            Long conversationId,
            Long currentUserId) {

        Conversation conversation =
                conversationMapper.selectById(conversationId);

        if (conversation == null) {
            throw new BusinessException(
                    26003,
                    "会话不存在"
            );
        }

        boolean participant =
                conversation.getUser1Id().equals(currentUserId)
                        || conversation.getUser2Id().equals(currentUserId);

        if (!participant) {
            throw new BusinessException(
                    26004,
                    "无权访问该会话"
            );
        }

        return conversation;
    }

    public PageResponse<MessageResponse> getMessages(
            Long conversationId,
            Long currentUserId,
            long page,
            long size) {

        if (page < 1 || size < 1 || size > 50) {
            throw new BusinessException(
                    26005,
                    "分页参数不合法"
            );
        }

        getAccessibleConversation(
                conversationId,
                currentUserId
        );

        Page<PrivateMessage> messagePage =
                privateMessageMapper.selectPage(
                        new Page<>(page, size),
                        new LambdaQueryWrapper<PrivateMessage>()
                                .eq(
                                        PrivateMessage::getConversationId,
                                        conversationId
                                )
                                .orderByDesc(
                                        PrivateMessage::getCreatedAt
                                )
                );

        List<MessageResponse> records =
                messagePage.getRecords()
                        .stream()
                        .map(message -> new MessageResponse(
                                message.getId(),
                                message.getSenderId(),
                                message.getContent(),
                                message.getCreatedAt(),
                                message.getSenderId()
                                        .equals(currentUserId)
                        ))
                        .toList();

        return new PageResponse<>(
                messagePage.getTotal(),
                page,
                size,
                records
        );
    }
}