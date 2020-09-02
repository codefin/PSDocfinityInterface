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
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

Author: Joe Finlinson - joe@beastute.com

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

metadata.date=

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

public class Psdocfinityinterface {

    private Properties properties = null;

    public Psdocfinityinterface(String propsFile){
        try {
            FileInputStream fis = new FileInputStream(new File(propsFile));
            properties = new Properties();
            properties.load(fis);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String uploadFile(String filePath, String operID){
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        StringBuilder sb = new StringBuilder();

        try{
            client = HttpClients.createDefault();
            HttpPost post = new HttpPost(properties.getProperty("upload.servlet.url"));
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getProperty("api.key"));
            String uuid = UUID.randomUUID().toString();
            post.setHeader("X-XSRF-TOKEN", uuid);
            post.setHeader("X-AUDITUSER", operID);
            post.setHeader("Cookie", "XSRF-TOKEN=" + uuid);

            File file = new File(filePath);
            HttpEntity data = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.LEGACY)
                    .addBinaryBody("upload_files", file, ContentType.DEFAULT_BINARY, file.getName())
                    .addTextBody("entryMethod", "", ContentType.DEFAULT_BINARY)
                    .build();
            post.setEntity(data);

            response = client.execute(post);

            System.out.println(response.toString());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
                sb.append(line);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(client != null){try{client.close();}catch(Exception e){e.printStackTrace();}}
            if(response != null){try{response.close();}catch(Exception e){e.printStackTrace();}}
        }
        return sb.toString();
    }

    public void indexMetadata(String docFinityID, String expenseId, String attachmentLoc,
                              String expenseLineId, String attachmentSequence, String operId, String date){
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try{
            client = HttpClients.createDefault();
            HttpPost post = new HttpPost(properties.getProperty("index.metadata.url"));
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getProperty("api.key"));
            String uuid = UUID.randomUUID().toString();
            post.setHeader("X-XSRF-TOKEN", uuid);
            post.setHeader("X-AUDITUSER", operId);
            post.setHeader("Cookie", "XSRF-TOKEN=" + uuid);
            post.setHeader("Content-Type", "application/json");

            HttpEntity entity = new StringEntity(buildMetadataJSON_(docFinityID, expenseId, attachmentLoc, expenseLineId, attachmentSequence, operId, date));
            post.setEntity(entity);

            response = client.execute(post);

            System.out.println(response.toString());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(client != null){try{client.close();}catch(Exception e){e.printStackTrace();}}
            if(response != null){try{response.close();}catch(Exception e){}}
        }
    }

    public void commitMetadata(String docFinityID, String operId){
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try{
            client = HttpClients.createDefault();
            HttpPost post = new HttpPost(properties.getProperty("commit.url"));
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getProperty("api.key"));
            String uuid = UUID.randomUUID().toString();
            post.setHeader("X-XSRF-TOKEN", uuid);
            post.setHeader("X-AUDITUSER", operId);
            post.setHeader("Cookie", "XSRF-TOKEN=" + uuid);
            post.setHeader("Content-Type", "application/json");

            com.beastute.commit.Root root = new com.beastute.commit.Root();
            root.id = "UPLOADS";
            ArrayList<String> list = new ArrayList<String>();
            list.add(docFinityID);
            root.documentIds = list;

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(root);
            System.out.println(json);
            HttpEntity entity = new StringEntity(json);
            post.setEntity(entity);

            response = client.execute(post);

            System.out.println(response.toString());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(client != null){try{client.close();}catch(Exception e){e.printStackTrace();}}
            if(response != null){try{response.close();}catch(Exception e){}}
        }
    }

    private String buildMetadataJSON_(String docFinityID, String expenseId, String attachmentLoc,
                                     String expenseLineId, String attachmentSequence, String operId, String date){
        String json = null;
        try {
            DocumentIndexingDTO myArray = new DocumentIndexingDTO();
            myArray.documentId = docFinityID;
            String documentTypeID = properties.getProperty("metadata.documentTypeId");
            myArray.documentTypeId = documentTypeID;
            myArray.addDto(buildIndexDto(properties.getProperty("metadata.expenseId"), documentTypeID, "STRING_VARIABLE", true, "ExpenseId", expenseId));
            myArray.addDto(buildIndexDto(properties.getProperty("metadata.attachmentLoc"), documentTypeID, "STRING_VARIABLE", true, "AttachmentLoc", attachmentLoc));
            myArray.addDto(buildIndexDto(properties.getProperty("metadata.expenseLineId"), documentTypeID, "STRING_VARIABLE", true, "ExpenseLineId", expenseLineId));
            myArray.addDto(buildIndexDto(properties.getProperty("metadata.attachmentSequence"), documentTypeID, "STRING_VARIABLE", true, "AttachmentSequence", attachmentSequence));
            myArray.addDto(buildIndexDto(properties.getProperty("metadata.userId"), documentTypeID, "STRING_VARIABLE", true, "UserId", operId));
            myArray.addDto(buildIndexDto(properties.getProperty("metadata.date"), documentTypeID, "DATE", true, "Date", date));

            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.writeValueAsString(new DocumentIndexingDTO[]{myArray});
            System.out.println(json);
        } catch (Exception e){
            e.printStackTrace();
        }
        return json;
    }

    private DocumentIndexingMetadataDto buildIndexDto(String metadataId, String docTypeId, String type,
                                                      boolean overrideError, String metadataName, String value){
        DocumentIndexingMetadataDto dto = new DocumentIndexingMetadataDto();
        dto.metadataId = metadataId;
        dto.documentTypeId = docTypeId;
        dto.type = type;
        dto.overrideError = overrideError;
        dto.metadataName = metadataName;
        dto.value = value;
        return dto;
    }

    public static void main(String[] args){
        Psdocfinityinterface docfinity = new Psdocfinityinterface("/Users/jfinlins/Downloads/docfinity.properties");
        String docId = docfinity.uploadFile("/Users/jfinlins/Downloads/test.pdf", "JFINLINS");
        docfinity.indexMetadata(docId, "1", "L", "1", "2", "JFINLINS", System.currentTimeMillis()+"");
        docfinity.commitMetadata(docId, "JFINLINS");
    }
}
