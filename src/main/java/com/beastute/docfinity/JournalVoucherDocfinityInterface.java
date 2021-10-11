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
                              String ledgerGroup, String reversal, String source, String unpostSequence) throws Exception {
        DocumentIndexingDTO documentIndexingDTO = new DocumentIndexingDTO();
        documentIndexingDTO.documentId = docFinityID;
        String documentTypeID = properties.getProperty("metadata.journals.documentTypeId");
        documentIndexingDTO.documentTypeId = documentTypeID;
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.accountingPeriod"), documentTypeID, "STRING_VARIABLE", "AccountingPeriod", accountingPeriod));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.attachmentLoc"), documentTypeID, "STRING_VARIABLE", "AttachmentLoc", attachmentLoc));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.attachmentSequence"), documentTypeID, "STRING_VARIABLE", "AttachmentSequence", attachmentSequence));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.businessUnit"), documentTypeID, "STRING_VARIABLE", "BusinessUnit", businessUnit));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.description"), documentTypeID, "STRING_VARIABLE", "Description", description));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.enteredBy"), documentTypeID, "STRING_VARIABLE", "EnteredBy", enteredBy));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.fiscalYear"), documentTypeID, "STRING_VARIABLE", "FiscalYear", fiscalYear));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.journalDate"), documentTypeID, "DATE", "JournalDate", journalDate));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.journalId"), documentTypeID, "STRING_VARIABLE", "JournalId", journalId));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.ledgerGroup"), documentTypeID, "STRING_VARIABLE", "LedgerGroup", ledgerGroup));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.reversal"), documentTypeID, "STRING_VARIABLE", "Reversal", reversal));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.source"), documentTypeID, "STRING_VARIABLE", "Source", source));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.unpostSequence"), documentTypeID, "STRING_VARIABLE", "UnpostSequence", unpostSequence));
        documentIndexingDTO.addDto(buildIndexDto(properties.getProperty("metadata.journals.userId"), documentTypeID, "STRING_VARIABLE", "userId", operId));
        super.indexMetadata(documentIndexingDTO, operId);
    }

    public static void main(String[] args) throws Exception {
        //This is to allow me to change this location without breaking your env.
        String propertiesFileLocation = System.getenv("propertiesFileLocation");

        for (int i = 0; i < 10; i++) {
            JournalVoucherDocfinityInterface jvdi = new JournalVoucherDocfinityInterface(propertiesFileLocation);

            String docId = jvdi.uploadFile("/Users/jfinlins/Downloads/test.pdf", "JFINLINS");

            //00000001fhr98z7ggk5w82fyvq5gfreg,1,1,DCROCKER,SPOKA,PAYROLL,DCROCKER,2021,PY,0210905,51631923200000,ACTUALS,N,PAY,0
           jvdi.indexMetadata(docId, "1", "1", "1", "DCROCKER", "SPOKA", "PAYROLL"+i, "DCROCKER", "2021", "51631923200000", "0210905", "ACTUALS", "N", "PAY", "0");

            jvdi.commitMetadata(docId, "JFINLINS");
        }

        //jvdi.deleteFile("00000001fhdw1k87d2hqpxmx41s4xj2x", "jfinlins");
    }
}
