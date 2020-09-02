package com.beastute.index;

import com.beastute.index.DocumentIndexingMetadataDto;

import java.util.ArrayList;
import java.util.List;

public class DocumentIndexingDTO {
    public String documentId;
    public String documentTypeId;
    public List<DocumentIndexingMetadataDto> documentIndexingMetadataDtos;

    public DocumentIndexingDTO(){
        documentIndexingMetadataDtos = new ArrayList<DocumentIndexingMetadataDto>();
    }

    public void addDto(DocumentIndexingMetadataDto dto){
        documentIndexingMetadataDtos.add(dto);
    }
}
