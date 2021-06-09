package com.beastute.index;

import java.util.ArrayList;
import java.util.List;

public class Root {
    public List<DocumentIndexingDTO> documentIndexingDTOs;

    public Root(){
        documentIndexingDTOs = new ArrayList<DocumentIndexingDTO>();
    }

    public void add(DocumentIndexingDTO myArray){
        this.documentIndexingDTOs.add(myArray);
    }
}
