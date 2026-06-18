package com.clipflow.message.service;

import com.clipflow.message.mapper.ConversationMapper;
import com.clipflow.message.mapper.PrivateMessageMapper;
import com.clipflow.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import com.clipflow.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import com.clipflow.message.entity.Conversation;
import com.clipflow.message.entity.PrivateMessage;
import com.clipflow.user.entity.User;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;

class MessageServiceTest {

    private ConversationMapper conversationMapper;
    private PrivateMessageMapper privateMessageMapper;
    private UserMapper userMapper;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        conversationMapper =
                Mockito.mock(ConversationMapper.class);

        privateMessageMapper =
                Mockito.mock(PrivateMessageMapper.class);

        userMapper =
                Mockito.mock(UserMapper.class);

        messageService = new MessageService(
                conversationMapper,
                privateMessageMapper,
                userMapper
        );
    }

    @Test
    void sendMessage_shouldRejectSendingToSelf() {

        BusinessException exception =
            assertThrows(
                BusinessException.class,
                () -> messageService.sendMessage(
                    12L,
                    12L,
                    "test"
                )
            );

        assertEquals(
                26001,
                exception.getCode()
        );

        assertEquals(
                "不能给自己发送私信",
                exception.getMessage()
        );

        verifyNoInteractions(
                userMapper,
                conversationMapper,
                privateMessageMapper
        );
    }

    @Test
    void sendMessage_shouldRejectMissingReceiver() {

        when(userMapper.selectById(99L))
                .thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> messageService.sendMessage(
                                12L,
                                99L,
                                "test"
                        )
                );

        assertEquals(
                26002,
                exception.getCode()
        );

        verify(userMapper)
                .selectById(99L);

        verifyNoInteractions(
                conversationMapper,
                privateMessageMapper
        );
    }

    @Test
    void sendMessage_shouldInsertMessageIntoExistingConversation() {

        User receiver = new User();
        receiver.setId(13L);

        Conversation conversation = new Conversation();
        conversation.setId(5L);
        conversation.setUser1Id(12L);
        conversation.setUser2Id(13L);

        when(userMapper.selectById(13L))
                .thenReturn(receiver);

        when(conversationMapper.selectOne(any()))
                .thenReturn(conversation);

        when(privateMessageMapper.insert(
                any(PrivateMessage.class)))
                .thenAnswer(invocation -> {
                    PrivateMessage message =
                            invocation.getArgument(0);

                    message.setId(100L);
                    return 1;
                });

        Long messageId = messageService.sendMessage(
                12L,
                13L,
                "hello"
        );

        assertEquals(100L, messageId);

        ArgumentCaptor<PrivateMessage> messageCaptor =
                ArgumentCaptor.forClass(
                        PrivateMessage.class
                );

        verify(privateMessageMapper)
                .insert(messageCaptor.capture());

        PrivateMessage insertedMessage =
                messageCaptor.getValue();

        assertEquals(
                5L,
                insertedMessage.getConversationId()
        );

        assertEquals(
                12L,
                insertedMessage.getSenderId()
        );

        assertEquals(
                "hello",
                insertedMessage.getContent()
        );

        verify(conversationMapper)
                .updateById(conversation);
    }
}