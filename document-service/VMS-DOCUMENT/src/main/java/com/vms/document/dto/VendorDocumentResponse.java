package com.vms.document.dto;
import java.util.List;
import com.vms.document.entity.Document;
import lombok.Data;
@Data
public class VendorDocumentResponse {
    private String vendorId;
    private String vendorName;
    private List<Document> documents;
}
