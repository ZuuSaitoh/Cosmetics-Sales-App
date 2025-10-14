package com.example.demo.repository;

import com.example.demo.entity.ChatMessages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Integer> {
}
