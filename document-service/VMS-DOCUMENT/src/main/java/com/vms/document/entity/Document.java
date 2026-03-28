package com.vms.document.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "documents")
@Data
public class Document {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID vendorId;
    private String fileName;
    private String fileKey;
    private String documentType;
    private LocalDateTime uploadedAt;

    // ✅ ADDED: approval status — PENDING / APPROVED / REJECTED
    private String status = "PENDING";
}