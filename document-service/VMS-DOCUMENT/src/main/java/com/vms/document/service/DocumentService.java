package com.vms.document.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.vms.document.client.VendorClient;
import com.vms.document.dto.VendorDocumentResponse;
import com.vms.document.dto.VendorResponse;
import com.vms.document.entity.Document;
import com.vms.document.repository.DocumentRepository;
import com.vms.document.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository repository;
    private final VendorClient vendorClient;
    private final S3Service s3Service;
    public Document saveMetadata(UUID vendorId, String fileName, String fileKey, String docType) {
        Document doc = new Document();
        doc.setVendorId(vendorId);
        doc.setFileName(fileName);
        doc.setFileKey(fileKey);
        doc.setDocumentType(docType);
        doc.setUploadedAt(LocalDateTime.now());
        return repository.save(doc);
    }
    public List<Document> getByVendor(UUID vendorId) {
        return repository.findByVendorId(vendorId);
    }
    public Document approve(UUID id) {
        Document doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));
        doc.setStatus("APPROVED");
        return repository.save(doc);
    }
    public Document reject(UUID id) {
        Document doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));
        doc.setStatus("REJECTED");
        return repository.save(doc);
    }
    public String getDownloadUrl(UUID id) {
        Document doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));
        return s3Service.generateDownloadUrl(doc.getFileKey());
    }
    public List<VendorDocumentResponse> getDocumentsGroupedByVendor() {
        List<Document> docs = repository.findAll();
        Map<UUID, List<Document>> grouped =
                docs.stream().collect(Collectors.groupingBy(Document::getVendorId));
        return grouped.entrySet().stream().map(entry -> {
            UUID vendorId = entry.getKey();
            String vendorName = "Unknown Vendor";
            try {
                VendorResponse vendor = vendorClient.getVendor(vendorId.toString());
                if (vendor != null && vendor.getCompanyName() != null) {
                    vendorName = vendor.getCompanyName();
                }
            } catch (Exception e) {
                log.warn("Vendor service unavailable for id: {}", vendorId);
            }
            VendorDocumentResponse res = new VendorDocumentResponse();
            res.setVendorId(vendorId.toString());
            res.setVendorName(vendorName);
            res.setDocuments(entry.getValue());
            return res;
        }).toList();
    }
}