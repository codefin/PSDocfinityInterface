package com.beastute.conversion;

import com.beastute.docfinity.JournalVoucherDocfinityInterface;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class JournalConversion {
    public void convertJournals(){

        JournalVoucherDocfinityInterface jvdi = new JournalVoucherDocfinityInterface(System.getenv("propfile"));

        int docCount = 1;

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Instant start;
        Instant end;
        ArrayList<Long> durations = new ArrayList<>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        PreparedStatement attachmentInfoPstmt = null;
        ResultSet attachmentInfoRset = null;

        PreparedStatement moreAttInfoPstmt = null;
        ResultSet moreAttInfoRset = null;

        PreparedStatement getFilePstmt = null;
        ResultSet getFileRset = null;

        PreparedStatement customTablePstmt = null;

        PreparedStatement removeAttachmentPstmt = null;

        try{
          String serverConnectString = System.getenv("serverConnectString");
          connection= DriverManager.getConnection(serverConnectString);
          statement = connection.createStatement();
          resultSet = statement.executeQuery("""
                SELECT A.BUSINESS_UNIT
                 , A.JOURNAL_ID
                 , A.JOURNAL_DATE
                 , A.UNPOST_SEQ
                 , A.ACCOUNTING_PERIOD
                 , A.DESCR254
                 , A.SOURCE
                 , A.REVERSAL_CD
                 , A.LEDGER_GROUP
                 , A.FISCAL_YEAR
                 , A.OPRID
                  FROM PS_JRNL_HEADER A
                 WHERE
                 A.FISCAL_YEAR = '2018'
                 AND NOT EXISTS (
                 SELECT 'X'
                  FROM PS_SPO_GL_DOCF_TBL B
                 WHERE A.BUSINESS_UNIT = B.BUSINESS_UNIT
                   AND A.JOURNAL_ID = B.JOURNAL_ID
                   AND A.JOURNAL_DATE = B.JOURNAL_DATE
                   AND A.UNPOST_SEQ = B.UNPOST_SEQ)
                   AND EXISTS (
                 SELECT 'X'
                  FROM PS_JRNL_HEADER_ATT C
                 WHERE A.BUSINESS_UNIT = C.BUSINESS_UNIT
                   AND A.JOURNAL_ID = C.JOURNAL_ID
                   AND A.JOURNAL_DATE = C.JOURNAL_DATE
                   AND A.UNPOST_SEQ = C.UNPOST_SEQ)
               """);
          if(resultSet != null) {
              while(resultSet.next())
              {
                  start = Instant.now();
                  //gives me a journal to convert
                  String businessUnit = resultSet.getString("BUSINESS_UNIT");
                  String journalId = resultSet.getString("JOURNAL_ID");
                  Date journalDate = resultSet.getDate("JOURNAL_DATE");
                  String unpostSeq = resultSet.getString("UNPOST_SEQ");
                  String accountingPeriod = resultSet.getString("ACCOUNTING_PERIOD");
                  String descr254 = resultSet.getString("DESCR254");
                  String source = resultSet.getString("SOURCE");
                  String reversalCode = resultSet.getString("REVERSAL_CD");
                  String ledgerGroup = resultSet.getString("LEDGER_GROUP");
                  String fiscalYear = resultSet.getString("FISCAL_YEAR");
                  String operId = resultSet.getString("OPRID");

                  attachmentInfoPstmt = connection.prepareStatement("""
                            SELECT SCM_ATTACH_ID
                            , ATT_VERSION
                            FROM PS_JRNL_HEADER_ATT
                            WHERE BUSINESS_UNIT = (?)
                                AND JOURNAL_ID = (?)
                                AND JOURNAL_DATE = (?)
                                AND UNPOST_SEQ = (?)
                                AND SCM_ATTACH_ID NOT IN
                                 (SELECT SCM_ATTACH_ID
                                    FROM PS_SPO_GL_DOCF_TBL
                                    WHERE BUSINESS_UNIT = (?)
                                    AND JOURNAL_ID = (?)
                                    AND JOURNAL_DATE = (?)
                                    AND UNPOST_SEQ = (?))
                          """);
                  attachmentInfoPstmt.setString(1, businessUnit);
                  attachmentInfoPstmt.setString(2, journalId);
                  attachmentInfoPstmt.setDate(3, journalDate);
                  attachmentInfoPstmt.setString(4, unpostSeq);
                  attachmentInfoPstmt.setString(5, businessUnit);
                  attachmentInfoPstmt.setString(6, journalId);
                  attachmentInfoPstmt.setDate(7, journalDate);
                  attachmentInfoPstmt.setString(8, unpostSeq);
                  attachmentInfoPstmt.execute();
                  attachmentInfoRset = attachmentInfoPstmt.getResultSet();
                  if(attachmentInfoRset != null)
                      while(attachmentInfoRset.next()){
                          String attachmentId = attachmentInfoRset.getString("SCM_ATTACH_ID");
                          String attachmentVersion = attachmentInfoRset.getString("ATT_VERSION");

                          moreAttInfoPstmt = connection.prepareStatement("""
                                  SELECT ATTACHSYSFILENAME
                                  , ATTACHUSERFILE
                                  FROM PS_PV_ATTACHMENTS
                                  WHERE SCM_ATTACH_ID = ?
                                  AND ATT_VERSION = ?
                                  """);
                          moreAttInfoPstmt.setString(1, attachmentId);
                          moreAttInfoPstmt.setString(2, attachmentVersion);
                          moreAttInfoRset = moreAttInfoPstmt.executeQuery();
                          if(moreAttInfoRset != null)
                              while(moreAttInfoRset.next()) {
                                  String sysfilename = moreAttInfoRset.getString("ATTACHSYSFILENAME");
                                  String usrfilename = moreAttInfoRset.getString("ATTACHUSERFILE");
                                  getFilePstmt = connection.prepareStatement("""
                                          SELECT FILE_DATA
                                          FROM PS_PV_ATT_DB_SRV
                                          WHERE ATTACHSYSFILENAME = ?
                                          """);
                                  getFilePstmt.setString(1, sysfilename);
                                  getFileRset = getFilePstmt.executeQuery();
                                  String path = System.getenv("writePath") + usrfilename;
                                  if (getFileRset != null)
                                      while (getFileRset.next()) {
                                          //peoplesoft stores the binary for a file across multiple records
                                          InputStream is = getFileRset.getBinaryStream("FILE_DATA");
                                          OutputStream os = new FileOutputStream(path, true);
                                          int c;
                                          while ((c = is.read()) > -1) {
                                              os.write(c);
                                          }
                                          os.close();
                                      }
                                  Path p = new File(path).toPath();
                                  String docId = "not_converted";
                                  if(Files.exists(p)) {
                                      docId = jvdi.uploadFile(path, operId);
                                      jvdi.indexMetadata(docId, accountingPeriod, "h", "1", operId, businessUnit, descr254, operId, fiscalYear, journalDate.getTime() + "", journalId, ledgerGroup, reversalCode, source, unpostSeq);
                                      jvdi.commitMetadata(docId, operId);
                                      File file = new File(path);
                                      if(!file.delete())
                                          System.out.println("Unable to delete file from filesystem: " + path);

                                      customTablePstmt = connection.prepareStatement("""
                                              INSERT INTO PS_SPO_GL_DOCF_TBL
                                              VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                                              """);
                                      customTablePstmt.setString(1, attachmentId);
                                      customTablePstmt.setString(2, attachmentVersion);
                                      customTablePstmt.setString(3, sysfilename);
                                      customTablePstmt.setString(4, businessUnit);
                                      customTablePstmt.setString(5, journalId);
                                      customTablePstmt.setDate(6, journalDate);
                                      customTablePstmt.setString(7, unpostSeq);
                                      customTablePstmt.setString(8, accountingPeriod);
                                      customTablePstmt.setString(9, source);
                                      customTablePstmt.setString(10, reversalCode);
                                      customTablePstmt.setString(11, ledgerGroup);
                                      customTablePstmt.setString(12, fiscalYear);
                                      customTablePstmt.setString(13, operId);
                                      customTablePstmt.setString(14, operId);
                                      customTablePstmt.setString(15, "H");
                                      customTablePstmt.setString(16, operId);
                                      customTablePstmt.setString(17, " ");
                                      customTablePstmt.setString(18, docId);
                                      customTablePstmt.setString(19, descr254);
                                      customTablePstmt.executeUpdate();

                                      removeAttachmentPstmt = connection.prepareStatement("""
                                              DELETE FROM PS_PV_ATT_DB_SRV
                                              WHERE ATTACHSYSFILENAME = ?
                                              """);
                                      removeAttachmentPstmt.setString(1, sysfilename);
                                      removeAttachmentPstmt.executeUpdate();
                                  }
                                  end = Instant.now();
                                  Duration d = Duration.between(start, end);
                                  long time = d.getSeconds();
                                  durations.add(time);
                                  long sum = 0;
                                  for(long l : durations){
                                      sum = sum + l;
                                  }
                                  double average = (double)sum/durations.size();

                                  System.out.println(dateFormat.format(new Date(System.currentTimeMillis())) + " Converted " + usrfilename + " " + docId + " " + docCount++ + " " + time + "s " + average + "s");
                              }
                      }
              }
          }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if(removeAttachmentPstmt != null)try{removeAttachmentPstmt.close();}catch (Exception e){e.printStackTrace();}
            if(customTablePstmt != null)try{customTablePstmt.close();}catch (Exception e){e.printStackTrace();}
            if(getFileRset != null)try{getFileRset.close();}catch (Exception e){e.printStackTrace();}
            if(getFilePstmt != null)try{getFilePstmt.close();}catch (Exception e){e.printStackTrace();}
            if(moreAttInfoRset != null)try{moreAttInfoRset.close();}catch (Exception e){e.printStackTrace();}
            if(moreAttInfoPstmt != null)try{moreAttInfoPstmt.close();}catch (Exception e){e.printStackTrace();}
            if(attachmentInfoRset != null)try{attachmentInfoRset.close();}catch (Exception e){e.printStackTrace();}
            if(attachmentInfoPstmt != null)try{attachmentInfoPstmt.close();}catch (Exception e){e.printStackTrace();}
            if(resultSet != null)try{resultSet.close();}catch (Exception e){e.printStackTrace();}
            if(statement != null)try{statement.close();}catch (Exception e){e.printStackTrace();}
            if(connection != null)try{connection.close();}catch (Exception e){e.printStackTrace();}
        }
    }

    public static void main(String [] args){
        JournalConversion jc = new JournalConversion();
        jc.convertJournals();
    }
}
