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

public class JournalVoucherDocfinityInterface extends BaseDocfinityInterface {

    public JournalVoucherDocfinityInterface(String propsFile) {
        super((propsFile));
    }

    public void indexMetadata(String docFinityID, String accountingPeriod, String attachmentLoc,
                              String attachmentSequence, String operId, String businessUnit, String description,
                              String enteredBy, String fiscalYear, String journalDate, String journalId,
                              String ledgerGroup, String reversal, String source, String unpostSequence,
                              String employeeId) throws Exception {
        DocumentIndexingDTO documentIndexingDTO = new DocumentIndexingDTO();
        documentIndexingDTO.documentId = docFinityID;
        String documentTypeID = properties.getProperty("metadata.documentTypeId");
        documentIndexingDTO.documentTypeId = documentTypeID;

        super.indexMetadata(documentIndexingDTO, operId);
    }

    public static void main(String[] args) throws Exception {
        //This is to allow me to change this location without breaking your env.
        String propertiesFileLocation = System.getenv("propertiesFileLocation");
        if (propertiesFileLocation == null) {
            propertiesFileLocation = "/Users/jfinlins/Downloads/docfinity/docfinity.properties";
        }
        //This is to allow me to change this location without breaking your env.
        String uploadFileLocation = System.getenv("uploadFileLocation");
        if (uploadFileLocation == null) {
            uploadFileLocation = "/Users/jfinlins/Downloads/docfinity/test.pdf";
        }

        String operId = "JFINLINS";
        JournalVoucherDocfinityInterface docfinity = new JournalVoucherDocfinityInterface(propertiesFileLocation);
        String docId = docfinity.uploadFile(uploadFileLocation, operId);
        System.out.println(docId);
//        docfinity.indexMetadata(docId, "1", "L", "1", "2", operId, "",
//                "", "", "", "", "", "", "", "");
        docfinity.commitMetadata(docId, operId);

        docfinity.deleteFile(docId, operId);
    }
}
