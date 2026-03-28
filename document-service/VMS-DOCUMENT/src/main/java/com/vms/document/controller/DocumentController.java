package com.vms.document.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vms.document.dto.VendorDocumentResponse;
import com.vms.document.entity.Document;
import com.vms.document.repository.DocumentRepository;
import com.vms.document.service.DocumentService;
import com.vms.document.service.S3Service;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired private S3Service s3Service;
    @Autowired private DocumentService documentService;
    @Autowired private DocumentRepository repository;

    // 1. Generate pre-signed S3 upload URL
    @PostMapping("/upload")
    public ResponseEntity<?> getUploadUrl(@RequestBody Map<String, String> req) {
        String fileName = req.get("fileName");
        String contentType = req.get("contentType");
        UUID vendorId = UUID.fromString(req.get("vendorId"));
        return ResponseEntity.ok(s3Service.generateUploadUrl(fileName, contentType, vendorId));
    }

    // 2. Save document metadata after upload
    @PostMapping
    public ResponseEntity<?> saveMetadata(@RequestBody Map<String, String> req) {
        Document doc = documentService.saveMetadata(
                UUID.fromString(req.get("vendorId")),
                req.get("fileName"),
                req.get("fileKey"),
                req.get("docType")
        );
        return ResponseEntity.ok(doc);
    }

    // 3. Get all documents (admin / procurement)
    @GetMapping
    public List<Document> getAll() {
        return repository.findAll();
    }

    // ✅ 4. Get documents for a specific vendor (vendor self-view)
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Document>> getByVendor(@PathVariable UUID vendorId) {
        return ResponseEntity.ok(documentService.getByVendor(vendorId));
    }

    // 5. Grouped view
    @GetMapping("/grouped")
    public List<VendorDocumentResponse> getGroupedDocs() {
        return documentService.getDocumentsGroupedByVendor();
    }

    // 6. Approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.approve(id));
    }

    // 7. Reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.reject(id));
    }

    // 8. Download
    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getDownloadUrl(id));
    }
}