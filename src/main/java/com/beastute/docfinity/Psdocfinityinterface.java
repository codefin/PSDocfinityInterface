/*
MIT License

Copyright (c) 2020 Astute Business Solutions

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

Instantiation of this class requires a properties file that contains information
regarding meta-data ids and URL locations for calls
Please find a sample properties file below:

#Psdocfinity properties file

upload.servlet.url=https://cloud.docfinity.com/<cust-id>/servlet/upload

api.key=

index.metadata.url=https://cloud.docfinity.com/<custId>/webservices/rest/indexing/index

metadata.documentTypeId=

metadata.expenseId=

metadata.attachmentLoc=

metadata.expenseLineId=

metadata.attachmentSequence=

metadata.userId=

metadata.employeeId=

metadata.date=

metadata.creationDate=

metadata.businessPurpose=

metadata.description=

metadata.zipCode=

metadata.Reference=

metadata.fromDate=

metadata.toDate=

commit.url=https://cloud.docfinity.com/<cust_id>/webservices/rest/indexing/commit

 */

package com.beastute.docfinity;

import com.beastute.index.DocumentIndexingDTO;
import com.beastute.index.DocumentIndexingMetadataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

public class Psdocfinityinterface extends BaseDocfinityInterface {

    public Psdocfinityinterface(String propsFile){
        super((propsFile));
    }

    public void indexMetadata(String docFinityID, String expenseId, String attachmentLoc,
                              String expenseLineId, String attachmentSequence, String operId, String date, String businessPurpose,
                              String description, String zipCode, String reference, String fromDate, String toDate, String creationDate,
                              String employeeId) throws Exception {

        DocumentIndexingDTO documentIndexingDTO = new DocumentIndexingDTO();
        documentIndexingDTO.documentId = docFinityID;
        String documentTypeID = properties.getProperty("metadata.documentTypeId");
        documentIndexingDTO.documentTypeId = documentTypeID;
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.expenseId"), documentTypeID, "STRING_VARIABLE", "ExpenseId", expenseId));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.attachmentLoc"), documentTypeID, "STRING_VARIABLE", "AttachmentLoc", attachmentLoc));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.expenseLineId"), documentTypeID, "STRING_VARIABLE", "ExpenseLineId", expenseLineId));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.attachmentSequence"), documentTypeID, "STRING_VARIABLE", "AttachmentSequence", attachmentSequence));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.userId"), documentTypeID, "STRING_VARIABLE", "UserId", operId));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.date"), documentTypeID, "DATE", "Date", date));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.employeeId"), employeeId, "STRING_VARIABLE", "EmployeeId", employeeId));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.creationDate"), creationDate, "DATE", "CreationDate", creationDate));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.businessPurpose"), businessPurpose, "STRING_VARIABLE", "BusinessPurpose", businessPurpose));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.description"), description, "STRING_VARIABLE", "Description", description));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.zipCode"), description, "STRING_VARIABLE", "ZipCode", zipCode));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.Reference"), description, "STRING_VARIABLE", "Reference", reference));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.fromDate"), creationDate, "DATE", "FromDate", fromDate));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.toDate"), creationDate, "DATE", "ToDate", toDate));

        super.indexMetadata(documentIndexingDTO, operId);
    }

    public static void main(String[] args) throws Exception {
        //This is to allow me to change this location without breaking your env.
        String propertiesFileLocation = System.getenv("propertiesFileLocation");
        if(propertiesFileLocation == null) {
            propertiesFileLocation = "/Users/jfinlins/Downloads/docfinity/docfinity.properties";
        }
        //This is to allow me to change this location without breaking your env.
        String uploadFileLocation = System.getenv("uploadFileLocation");
        if(uploadFileLocation == null) {
            uploadFileLocation = "/Users/jfinlins/Downloads/docfinity/test.pdf";
        }

        String operId = "JFINLINS";
        Psdocfinityinterface docfinity = new Psdocfinityinterface(propertiesFileLocation);
        String docId = docfinity.uploadFile(uploadFileLocation, operId);
        System.out.println(docId);
        docfinity.indexMetadata(docId, "1", "L", "1", "2", operId, "",
                "", "", "", "", "", "", "", "");
        docfinity.commitMetadata(docId, operId);

        docfinity.deleteFile(docId, operId);
    }
}
