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

public class BaseDocfinityInterface {

    protected Properties properties = null;

    protected BaseDocfinityInterface(String propsFile) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(propsFile));
            properties = new Properties();
            properties.load(fis);

        }catch (Exception e){
            e.printStackTrace();
        }
        finally{
            if(fis != null){try{fis.close();}catch(Exception e){}}
        }
    }

    public String uploadFile(String filePath, String operID) throws Exception {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        StringBuilder sb = null;
        HttpPost post = null;
        MultipartEntityBuilder meb = null;
        HttpEntity me = null;
        HttpEntity responseEntity = null;
        InputStreamReader isr = null;
        BufferedReader rd = null;
        FileInputStream fis = null;
        File f = null;

        try{
            client = HttpClients.createDefault();
            post = new HttpPost(properties.getProperty("upload.servlet.url"));
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getProperty("api.key"));
            String uuid = UUID.randomUUID().toString();
            post.setHeader("X-XSRF-TOKEN", uuid);
            post.setHeader("X-AUDITUSER", operID);
            post.setHeader("Cookie", "XSRF-TOKEN=" + uuid);

            f = new File(filePath);
            fis = new FileInputStream(f);
            //data = MultipartEntityBuilder.create()
            //        .setMode(HttpMultipartMode.LEGACY)
            //        .addBinaryBody("upload_files", file, ContentType.DEFAULT_BINARY, file.getName())
            //        .addTextBody("entryMethod", "", ContentType.DEFAULT_BINARY)
            //        .build();
            //post.setEntity(data);
            meb = MultipartEntityBuilder.create();
            meb.addTextBody("entryMethod", "", ContentType.TEXT_PLAIN);
            meb.addBinaryBody("file", fis, ContentType.APPLICATION_OCTET_STREAM, f.getName());
            me = meb.build();
            post.setEntity(me);

            response = client.execute(post);

            int status = response.getCode();
            System.out.println("docfinity upload api- HTTP RESPONSE CODE: " + response);
            if(status != 204 && status != 200) {
                throw new Exception("Unable to create document." + filePath + " user " + operID + " HTTP RESPONSE CODE: " + response);
            }
            responseEntity = response.getEntity();
            isr = new InputStreamReader(responseEntity.getContent());
            rd = new BufferedReader(isr);
            String line;
            sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
                sb.append(line);
            }
        } finally {
            if(client != null){try{client.close();}catch(Exception e){}}
            if(response != null){try{response.close();}catch(Exception e){}}
            if(post != null){try{post.clear();}catch(Exception e){}}
            if(me != null){try{me.close();}catch(Exception e){}}
            if(responseEntity != null){try{responseEntity.close();}catch(Exception e){}}
            if(isr != null){try{isr.close();}catch(Exception e){}}
            if(rd != null){try{rd.close();}catch(Exception e){}}
            if(fis != null){try{fis.close();}catch(Exception e){}}
        }
        return sb.toString();
    }

    protected void indexMetadata(DocumentIndexingDTO documentIndexingDTO, String operId) throws Exception {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        HttpPost post = null;
        BufferedReader rd = null;
        InputStreamReader isr = null;
        HttpEntity responseEntity = null;

        try{
            client = HttpClients.createDefault();
            post = new HttpPost(properties.getProperty("index.metadata.url"));
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getProperty("api.key"));
            String uuid = UUID.randomUUID().toString();
            post.setHeader("X-XSRF-TOKEN", uuid);
            post.setHeader("X-AUDITUSER", operId);
            post.setHeader("Cookie", "XSRF-TOKEN=" + uuid);
            post.setHeader("Content-Type", "application/json");

            entity = new StringEntity(buildMetadataJSON_(documentIndexingDTO));
            post.setEntity(entity);

            response = client.execute(post);

            responseEntity = response.getEntity();
            isr = new InputStreamReader(responseEntity.getContent());
            rd = new BufferedReader(isr);

            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }

            int status = response.getCode();
            System.out.println("docfinity index metadata api- HTTP RESPONSE CODE: " + response);
            if(status != 204 && status != 200) {
                throw new Exception("Unable to index metadata. docfinityId" + documentIndexingDTO.documentId + "operId " + operId + " HTTP RESPONSE CODE: " + response);
            }
        }finally{
            if(client != null){try{client.close();}catch(Exception e){}}
            if(response != null){try{response.close();}catch(Exception e){}}
            if(post != null){try{post.clear();}catch(Exception e){}}
            if(entity != null){try{entity.close();}catch(Exception e){}}
            if(rd != null){try{rd.close();}catch(Exception e){}}
            if(isr != null){try{isr.close();}catch(Exception e){}}
            if(responseEntity != null){try{responseEntity.close();}catch(Exception e){}}
        }
    }

    protected void reindexMetadata(DocumentIndexingDTO documentIndexingDTO, String operId) throws Exception {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try{
            client = HttpClients.createDefault();
            HttpPost post = new HttpPost(properties.getProperty("reindex.metadata.url"));
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getProperty("api.key"));
            String uuid = UUID.randomUUID().toString();
            post.setHeader("X-XSRF-TOKEN", uuid);
            post.setHeader("X-AUDITUSER", operId);
            post.setHeader("Cookie", "XSRF-TOKEN=" + uuid);
            post.setHeader("Content-Type", "application/json");

            HttpEntity entity = new StringEntity(buildMetadataJSON_(documentIndexingDTO));
            post.setEntity(entity);

            response = client.execute(post);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }

            int status = response.getCode();
            System.out.println("docfinity reindex metadata api- HTTP RESPONSE CODE: " + response);
            if(status != 204 && status != 200) {
                throw new Exception("Unable to reindex metadata. docfinityId" + documentIndexingDTO.documentId + "operId " + operId + " HTTP RESPONSE CODE: " + response);
            }
        }finally{
            if(client != null){try{client.close();}catch(Exception e){}}
            if(response != null){try{response.close();}catch(Exception e){}}
        }
    }

    public void deleteFile(String docFinityID, String operID) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try{
            client = HttpClients.createDefault();
            HttpPost post = new HttpPost(properties.getProperty("delete.servlet.url"));
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getProperty("api.key"));
            String uuid = UUID.randomUUID().toString();
            post.setHeader("X-XSRF-TOKEN", uuid);
            post.setHeader("X-AUDITUSER", operID);
            post.setHeader("Cookie", "XSRF-TOKEN=" + uuid);
            post.setHeader("Content-Type", "application/json");
            String deleteId = "[\"" + docFinityID + "\"]";
            System.out.println(deleteId);
            HttpEntity entity = new StringEntity(deleteId);

            post.setEntity(entity);

            response = client.execute(post);
            int status = response.getCode();
            System.out.println("docfinity delete api- HTTP RESPONSE CODE: " + response);
            if(status != 204 && status != 200) {
                throw new Exception("Unable to delete document. docfinityId" + docFinityID + "operId " + operID + " HTTP RESPONSE CODE: " + response);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(client != null){try{client.close();}catch(Exception e){}}
            if(response != null){try{response.close();}catch(Exception e){}}
        }
    }

    public void commitMetadata(String docFinityID, String operId) throws Exception {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        HttpPost post = null;
        BufferedReader rd = null;
        InputStreamReader isr = null;
        HttpEntity responseEntity = null;

        try{
            client = HttpClients.createDefault();
            post = new HttpPost(properties.getProperty("commit.url"));
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getProperty("api.key"));
            String uuid = UUID.randomUUID().toString();
            post.setHeader("X-XSRF-TOKEN", uuid);
            post.setHeader("X-AUDITUSER", operId);
            post.setHeader("Cookie", "XSRF-TOKEN=" + uuid);
            post.setHeader("Content-Type", "application/json");

            com.beastute.commit.Root root = new com.beastute.commit.Root();
            root.id = "UPLOADS";
            ArrayList<String> list = new ArrayList<>();
            list.add(docFinityID);
            root.documentIds = list;

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(root);
            System.out.println(json);
            entity = new StringEntity(json);
            post.setEntity(entity);

            response = client.execute(post);
            int status = response.getCode();
            System.out.println("docfinity commit api- HTTP RESPONSE CODE: " + status);

            System.out.println(response);

            responseEntity = response.getEntity();
            isr = new InputStreamReader(responseEntity.getContent());
            rd = new BufferedReader(isr);

            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
            if(status != 204 && status != 200) {
                throw new Exception("Unable to commit metadata document " + docFinityID + ".");
            }
        }finally{
            if(client != null){try{client.close();}catch(Exception e){}}
            if(response != null){try{response.close();}catch(Exception e){}}
            if(post != null){try{post.clear();}catch(Exception e){}}
            if(entity != null){try{entity.close();}catch(Exception e){}}
            if(rd != null){try{rd.close();}catch(Exception e){}}
            if(isr != null){try{isr.close();}catch(Exception e){}}
            if(responseEntity != null){try{responseEntity.close();}catch(Exception e){}}
        }
    }

    private String buildMetadataJSON_(DocumentIndexingDTO documentIndexingDTO){
        String json = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.writeValueAsString(new DocumentIndexingDTO[]{documentIndexingDTO});
            System.out.println(json);
        } catch (Exception e){
            e.printStackTrace();
        }
        return json;
    }

    protected DocumentIndexingMetadataDto buildIndexDto(String metadataId, String docTypeId, String type,
                                                      String metadataName, String value){
        DocumentIndexingMetadataDto dto = new DocumentIndexingMetadataDto();
        dto.metadataId = metadataId;
        dto.documentTypeId = docTypeId;
        dto.type = type;
        dto.overrideError = true;
        dto.metadataName = metadataName;
        dto.value = value;
        return dto;
    }
}
