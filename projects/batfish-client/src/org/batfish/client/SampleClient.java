package org.batfish.client;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.UUID;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.WorkItem;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.uri.UriComponent;

public class SampleClient {

   private String _workMgr;
   private String _poolMgr;

   public SampleClient(String workMgr, String poolMgr, String testrigName,
         String testrigZipfileName, String envName, String envZipfileName,
         String questionName, String questionFileName) {

      try {
         _workMgr = workMgr;
         _poolMgr = poolMgr;

         System.out.println("\nPress any key to add local batfish worker");
         System.in.read();

         addLocalBatfishWorker();

         System.out.println("\nPress any key to upload test rig:" + testrigName
               + " / " + testrigZipfileName);
         System.in.read();
         uploadTestrig(testrigName, testrigZipfileName);

         System.out.println("Press any key to trigger vendor specific parsing");
         System.in.read();
         doWork(testrigName, BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, "",
               BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR);

         System.out
               .println("Press any key to trigger vendor independent parsing");
         System.in.read();
         doWork(testrigName, BfConsts.COMMAND_PARSE_VENDOR_INDEPENDENT, "",
               BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR);

         System.out.println("Press any key to upload environment");
         System.in.read();
         uploadEnvironment(testrigName, envName, envZipfileName);

         System.out.println("Press any key to trigger fact generation");
         System.in.read();
         WorkItem wItem = new WorkItem(testrigName);
         wItem.addRequestParam(BfConsts.COMMAND_GENERATE_FACT, "");
         wItem.addRequestParam(BfConsts.COMMAND_ENV, envName);
         doWork(
               testrigName,
               wItem,
               Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName,
                     BfConsts.RELPATH_FACT_DUMP_DIR).toString());

         System.out.println("Press any key to generate the data plane");
         System.in.read();
         WorkItem wItem2 = new WorkItem(testrigName);
         wItem2.addRequestParam(BfConsts.COMMAND_COMPILE, "");
         wItem2.addRequestParam(BfConsts.COMMAND_FACTS, "");
         wItem2.addRequestParam(BfConsts.COMMAND_ENV, envName);
         doWork(testrigName, wItem2, null);

         System.out.println("Press any key to get the data plane");
         System.in.read();
         WorkItem wItem3 = new WorkItem(testrigName);
         wItem3.addRequestParam(BfConsts.COMMAND_DUMP_DP, "");
         wItem3.addRequestParam(BfConsts.COMMAND_ENV, envName);
         doWork(
               testrigName,
               wItem3,
               Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName,
                     BfConsts.RELPATH_DATA_PLANE_DIR).toString());

         System.out
               .println("Press any key to create the z3 data plane encoding");
         System.in.read();
         WorkItem wItem5 = new WorkItem(testrigName);
         wItem5.addRequestParam(BfConsts.COMMAND_SYNTHESIZE_Z3_DATA_PLANE, "");
         wItem5.addRequestParam(BfConsts.COMMAND_ENV, envName);
         doWork(
               testrigName,
               wItem5,
               Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName,
                     BfConsts.RELPATH_Z3_DATA_PLANE_FILE).toString());

         System.out.println("Press any key to upload question");
         System.in.read();
         uploadQuestion(testrigName, questionName, questionFileName);

         System.out.println("Press any key to answer the question");
         System.in.read();
         WorkItem wItem4 = new WorkItem(testrigName);
         wItem4.addRequestParam(BfConsts.COMMAND_ANSWER, "");
         wItem4.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
         doWork(testrigName, wItem4, null);

         System.out.println("Press any key to post the flows to LogicBlox");
         System.in.read();
         WorkItem wItem6 = new WorkItem(testrigName);
         wItem6.addRequestParam(BfConsts.COMMAND_POST_FLOWS, "");
         wItem6.addRequestParam(BfConsts.ARG_QUESTION_NAME, questionName);
         wItem6.addRequestParam(BfConsts.COMMAND_ENV, envName);
         doWork(testrigName, wItem6, null);

         System.out.println("Press any key to get flow traces");
         System.in.read();
         WorkItem wItem7 = new WorkItem(testrigName);
         wItem7.addRequestParam(BfConsts.COMMAND_QUERY, "");
         wItem7.addRequestParam(BfConsts.ARG_PREDICATES,
               BfConsts.PREDICATE_FLOW_PATH_HISTORY);
         wItem7.addRequestParam(BfConsts.COMMAND_ENV, envName);
         doWork(
               testrigName,
               wItem7,
               Paths.get(BfConsts.RELPATH_ENVIRONMENTS_DIR, envName,
                     BfConsts.RELPATH_QUERY_DUMP_DIR).toString());

      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void doWork(String testrigName, String commandKey,
         String commandValue, String resultsDir) throws Exception {

      WorkItem wItem = new WorkItem(testrigName);
      wItem.addRequestParam(commandKey, commandValue);

      doWork(testrigName, wItem, resultsDir);
   }

   private void doWork(String testrigName, WorkItem wItem, String resultsDir)
         throws Exception {

      // bad command for testing
      // wItem.addRequestParam("badcommand", "");

      queueWork(wItem);

      System.out.println("Press any key to start checking work status");
      System.in.read();

      WorkStatusCode status = getWorkStatus(wItem.getId());

      while (status != WorkStatusCode.TERMINATEDABNORMALLY
            && status != WorkStatusCode.TERMINATEDNORMALLY
            && status != WorkStatusCode.ASSIGNMENTERROR) {

         System.out.printf("status: %s\n", status);

         Thread.sleep(10 * 1000);

         status = getWorkStatus(wItem.getId());
      }

      System.out.printf("final status: %s\n", status);

      // System.out.println("Press any key to fetch results");
      // System.in.read();

      // get the results
      String logFile = wItem.getId() + ".log";
      // String logFile = "5ea3d4d3-682c-4c8b-8418-08f36fa3e638.log";
      getObject(testrigName, logFile);

      if (resultsDir != null)
         getObject(testrigName, resultsDir);
   }

   private boolean addLocalBatfishWorker() {
      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(
               String.format("http://%s%s/%s", _poolMgr,
                     CoordConsts.SVC_BASE_POOL_MGR,
                     CoordConsts.SVC_POOL_UPDATE_RSC)).queryParam("add",
               "localhost:" + BfConsts.SVC_PORT);
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         System.out.println(response.getStatus() + " "
               + response.getStatusInfo() + " " + response);

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.printf("Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            System.err.printf("got error while checking work status: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (ProcessingException e) {
         System.err.printf("unable to connect to %s: %s\n", _workMgr, e
               .getStackTrace().toString());
         return false;
      }
      catch (Exception e) {
         System.err.printf("exception: ");
         e.printStackTrace();
         return false;
      }
   }

   private WorkStatusCode getWorkStatus(UUID parseWorkUUID) {
      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(
               String.format("http://%s%s/%s", _workMgr,
                     CoordConsts.SVC_BASE_WORK_MGR,
                     CoordConsts.SVC_WORK_GET_WORKSTATUS_RSC)).queryParam(
               CoordConsts.SVC_WORKID_KEY,
               UriComponent.encode(parseWorkUUID.toString(),
                     UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         System.out.println(response.getStatus() + " "
               + response.getStatusInfo() + " " + response);

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.printf("Did not get an OK response\n");
            return null;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            System.err.printf("got error while checking work status: %s %s\n",
                  array.get(0), array.get(1));
            return null;
         }

         JSONObject jObj = new JSONObject(array.get(1).toString());

         if (!jObj.has(CoordConsts.SVC_WORKSTATUS_KEY)) {
            System.err.printf("workstatus key not found in: %s\n",
                  jObj.toString());
            return null;
         }

         return WorkStatusCode.valueOf(jObj
               .getString(CoordConsts.SVC_WORKSTATUS_KEY));
      }
      catch (ProcessingException e) {
         System.err.printf("unable to connect to %s: %s\n", _workMgr, e
               .getStackTrace().toString());
         return null;
      }
      catch (Exception e) {
         System.err.printf("exception: ");
         e.printStackTrace();
         return null;
      }
   }

   private boolean uploadTestrig(String testrigName, String zipfileName) {
      try {

         Client client = ClientBuilder.newBuilder()
               .register(MultiPartFeature.class).build();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               _workMgr, CoordConsts.SVC_BASE_WORK_MGR,
               CoordConsts.SVC_WORK_UPLOAD_TESTRIG_RSC));

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart testrigNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_TESTRIG_NAME_KEY, testrigName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(testrigNameBodyPart);

         FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
               CoordConsts.SVC_ZIPFILE_KEY, new File(zipfileName),
               MediaType.APPLICATION_OCTET_STREAM_TYPE);
         multiPart.bodyPart(fileDataBodyPart);

         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         System.out.println(response.getStatus() + " "
               + response.getStatusInfo() + " " + response);

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.printf("UploadTestrig: Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            System.err.printf("got error while uploading test rig: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (Exception e) {
         System.err.printf(
               "Exception when uploading test rig to %s using (%s, %s)\n",
               _workMgr, testrigName, zipfileName);
         e.printStackTrace();
         return false;
      }
   }

   private boolean uploadEnvironment(String testrigName, String envName,
         String zipfileName) {
      try {

         Client client = ClientBuilder.newBuilder()
               .register(MultiPartFeature.class).build();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               _workMgr, CoordConsts.SVC_BASE_WORK_MGR,
               CoordConsts.SVC_WORK_UPLOAD_ENV_RSC));

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart testrigNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_TESTRIG_NAME_KEY, testrigName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(testrigNameBodyPart);

         FormDataBodyPart envNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_ENV_NAME_KEY, envName, MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(envNameBodyPart);

         FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
               CoordConsts.SVC_ZIPFILE_KEY, new File(zipfileName),
               MediaType.APPLICATION_OCTET_STREAM_TYPE);
         multiPart.bodyPart(fileDataBodyPart);

         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         System.out.println(response.getStatus() + " "
               + response.getStatusInfo() + " " + response);

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err
                  .printf("UploadEnvironment: Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            System.err.printf("got error while uploading environment: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (Exception e) {
         System.err
               .printf(
                     "Exception when uploading environment to %s using (%s, %s, %s)\n",
                     _workMgr, testrigName, envName, zipfileName);
         e.printStackTrace();
         return false;
      }
   }

   private boolean uploadQuestion(String testrigName, String qName,
         String fileName) {
      try {

         Client client = ClientBuilder.newBuilder()
               .register(MultiPartFeature.class).build();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               _workMgr, CoordConsts.SVC_BASE_WORK_MGR,
               CoordConsts.SVC_WORK_UPLOAD_QUESTION_RSC));

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart testrigNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_TESTRIG_NAME_KEY, testrigName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(testrigNameBodyPart);

         FormDataBodyPart qNameBodyPart = new FormDataBodyPart(
               CoordConsts.SVC_QUESTION_NAME_KEY, qName,
               MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(qNameBodyPart);

         FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
               CoordConsts.SVC_FILE_KEY, new File(fileName),
               MediaType.APPLICATION_OCTET_STREAM_TYPE);
         multiPart.bodyPart(fileDataBodyPart);

         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         System.out.println(response.getStatus() + " "
               + response.getStatusInfo() + " " + response);

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.printf("UploadQuestion: Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            System.err.printf("got error while uploading environment: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (Exception e) {
         System.err.printf(
               "Exception when uploading question to %s using (%s, %s, %s)\n",
               _workMgr, testrigName, qName, fileName);
         e.printStackTrace();
         return false;
      }
   }

   private boolean queueWork(WorkItem wItem) {

      try {
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(
               String.format("http://%s%s/%s", _workMgr,
                     CoordConsts.SVC_BASE_WORK_MGR,
                     CoordConsts.SVC_WORK_QUEUE_WORK_RSC)).queryParam(
               CoordConsts.SVC_WORKITEM_KEY,
               UriComponent.encode(wItem.toJsonString(),
                     UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.printf("QueueWork: Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            System.err.printf("got error while queuing work: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (ProcessingException e) {
         System.err.printf("unable to connect to %s: %s\n", _workMgr, e
               .getStackTrace().toString());
         return false;
      }
      catch (Exception e) {
         System.err.printf("exception: ");
         e.printStackTrace();
         return false;
      }
   }

   private boolean getObject(String testrigName, String objectName) {
      try {

         Client client = ClientBuilder.newBuilder()
               .register(MultiPartFeature.class).build();
         WebTarget webTarget = client
               .target(
                     String.format("http://%s%s/%s", _workMgr,
                           CoordConsts.SVC_BASE_WORK_MGR,
                           CoordConsts.SVC_WORK_GET_OBJECT_RSC))
               .queryParam(CoordConsts.SVC_TESTRIG_NAME_KEY, testrigName)
               .queryParam(CoordConsts.SVC_WORK_OBJECT_KEY, objectName);

         Response response = webTarget.request(
               MediaType.APPLICATION_OCTET_STREAM).get();

         System.out.println(response.getStatus() + " "
               + response.getStatusInfo() + " " + response);

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.printf("GetObject: Did not get an OK response\n");
            return false;
         }

         // see if we have a filename header
         String outFileStr = objectName;

         MultivaluedMap<String, String> headers = response.getStringHeaders();

         if (headers.containsKey(CoordConsts.SVC_WORK_FILENAME_HDR)) {
            String value = headers.getFirst(CoordConsts.SVC_WORK_FILENAME_HDR);
            if (value != null && !value.equals("")) {
               outFileStr = value;
            }
         }

         File outdir = new File("client");
         outdir.mkdirs();

         File inFile = response.readEntity(File.class);
         File outFile = new File(outdir.getAbsolutePath() + "/" + outFileStr);

         inFile.renameTo(outFile);

         FileWriter fr = new FileWriter(inFile);
         fr.flush();
         fr.close();

         return true;
      }
      catch (Exception e) {
         System.err.printf(
               "Exception when uploading test rig to %s using (%s, %s)\n",
               _workMgr, testrigName, objectName);
         e.printStackTrace();
         return false;
      }
   }
}