package de.bund.digitalservice.a2j.service.egvp.client;

public record SendMessageRequest(
    String userId,
    String receiverId,
    String bundIdMailbox,
    String subject,
    String attachmentFile,
    String xJustizFile) {}