package org.batfish.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.WorkItem;

public class BatchClient {

   private BfCoordWorkHelper _workHelper;
   private BfCoordPoolHelper _poolHelper;
   private BatfishLogger _logger;
   
   public BatchClient(String workMgr, String poolMgr, String testrigName,
         String testrigZipfileName, String envName, String envZipfileName,
         String questionName, String questionFileName, String workerToAdd) {

      try {
    	  
    	  _logger = new BatfishLogger(BatfishLogger.getLogLevelStr(BatfishLogger.LEVEL_DEBUG), 
    			  						false, System.out);
         _workHelper = new BfCoordWorkHelper(workMgr, _logger);
         _poolHelper = new BfCoordPoolHelper(poolMgr);

         if (proceed("add local batfish worker")) {
            _poolHelper.addBatfishWorker(workerToAdd);
         }

         if (proceed("upload test rig:" + testrigName + " / "
               + testrigZipfileName)) {
            _workHelper.uploadTestrig(testrigName, testrigZipfileName);
         }

         if (proceed("parse vendor specific")) {
            execute(_workHelper.getWorkItemParseVendorSpecific(testrigName),
                  _workHelper.getResultsObjectNameParseVendorSpecific());
         }

         if (proceed("parse vendor independent")) {
            execute(_workHelper.getWorkItemParseVendorIndependent(testrigName),
                  _workHelper.getResultsObjectNameParseVendorIndependent());
         }

         if (proceed("upload environment")) {
            _workHelper.uploadEnvironment(testrigName, envName, envZipfileName);
         }

         if (proceed("generate facts")) {
            execute(_workHelper.getWorkItemGenerateFacts(testrigName, envName),
                  _workHelper.getResultsObjectNameGenerateFacts(envName));
         }

         if (proceed("generate the data plane")) {
            execute(_workHelper.getWorkItemGenerateDataPlane(testrigName,
                  envName),
                  _workHelper.getResultsObjectNameGenerateDataPlane(envName));
         }

         if (proceed("get the data plane")) {
            execute(_workHelper.getWorkItemGetDataPlane(testrigName, envName),
                  _workHelper.getResultsObjectNameGetDataPlane(envName));
         }

         if (proceed("create the z3 data plane encoding")) {
            execute(
                  _workHelper.getWorkItemCreateZ3Encoding(testrigName, envName),
                  _workHelper.getResultsObjectNameCreateZ3Encoding(envName));
         }

         if (proceed("upload question")) {
            _workHelper.uploadQuestion(testrigName, questionName,
                  questionFileName);
         }

         if (proceed("answer the question")) {
            execute(_workHelper.getWorkItemAnswerQuestion(testrigName, envName,
                  questionName),
                  _workHelper.getResultsObjectNameAnswerQuestion(envName,
                        questionName));
         }

         if (proceed("post the flows to LogicBlox")) {
            execute(_workHelper.getWorkItemPostFlows(testrigName, envName,
                  questionName), _workHelper.getResultsObjectNamePostFlows(
                  envName, questionName));
         }

         if (proceed("get flow traces")) {
            execute(_workHelper.getWorkItemGetFlowTraces(testrigName, envName,
                  questionName), _workHelper.getResultsObjectNameGetFlowTraces(
                  envName, questionName));
         }
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   private boolean proceed(String string) {
      System.out.print("\n" + string + " (y/N): ");
      try {
         BufferedReader bufferRead = new BufferedReader(new InputStreamReader(
               System.in));
         String s = bufferRead.readLine();

         if (s.startsWith("y") || s.startsWith("Y")) {
            return true;
         }
      }
      catch (Exception e) {
         e.printStackTrace();
      }
      return false;
   }

   private void execute(WorkItem wItem, String resultsObjName) throws Exception {

      _workHelper.queueWork(wItem);

      if (proceed("start checking work status")) {

         WorkStatusCode status = _workHelper.getWorkStatus(wItem.getId());

         while (status != WorkStatusCode.TERMINATEDABNORMALLY
               && status != WorkStatusCode.TERMINATEDNORMALLY
               && status != WorkStatusCode.ASSIGNMENTERROR) {

            System.out.printf("status: %s\n", status);

            Thread.sleep(10 * 1000);

            status = _workHelper.getWorkStatus(wItem.getId());
         }

         System.out.printf("final status: %s\n", status);
      }

      // get the results
      String logFile = wItem.getId() + ".log";
      // String logFile = "5ea3d4d3-682c-4c8b-8418-08f36fa3e638.log";
      _workHelper.getObject(wItem.getTestrigName(), logFile);

      if (resultsObjName != null)
         _workHelper.getObject(wItem.getTestrigName(), resultsObjName);
   }
}