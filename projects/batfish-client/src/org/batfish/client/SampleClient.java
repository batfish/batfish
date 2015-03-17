package org.batfish.client;

import java.io.File;
import java.io.FileNotFoundException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.batfish.common.CoordinatorConstants;
import org.batfish.common.WorkItem;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.uri.UriComponent;

public class SampleClient {

   private String _coordinator;
   
   public SampleClient(String coordinator) {

      _coordinator = coordinator;
      
      //DepositWork();
      
      UploadTestrig("samplerig", "sample.zip");
   }
   private boolean UploadTestrig(String testrigName, String zipfileName) {
      try {

         Client client = ClientBuilder.newBuilder()
               .register(MultiPartFeature.class).build();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               _coordinator, CoordinatorConstants.SERVICE_BASE,
               CoordinatorConstants.SERVICE_UPLOAD_TESTRIG));

         MultiPart multiPart = new MultiPart();
         multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

         FormDataBodyPart testrigNameBodyPart = new FormDataBodyPart(
               CoordinatorConstants.SERVICE_UPLOAD_TESTRIG_NAME_KEY,
               testrigName, MediaType.TEXT_PLAIN_TYPE);
         multiPart.bodyPart(testrigNameBodyPart);

         FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
               CoordinatorConstants.SERVICE_UPLOAD_TESTRIG_ZIPFILE_KEY,
               new File(zipfileName), MediaType.APPLICATION_OCTET_STREAM_TYPE);
         multiPart.bodyPart(fileDataBodyPart);

         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .post(Entity.entity(multiPart, multiPart.getMediaType()));

         System.out.println(response.getStatus() + " "
               + response.getStatusInfo() + " " + response);

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals("")) {
            System.err.printf("got error while uploading test rig: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }
         
         return true;
      }
      catch (Exception e) {
         System.err.printf("Exception when uploading test rig to %s using (%s, %s)\n", _coordinator, testrigName, zipfileName);
         e.printStackTrace();
         return false;
      }
   }

   private boolean DepositWork() {
      
      WorkItem wItem = new WorkItem();
      
      wItem.addRequestParam("command", "parse");
      wItem.addRequestParam("configlocation", "test_rigs_example");
      
      String wItemString = UriComponent.encode(wItem.toJsonString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED);
      
      try {                  
         Client client = ClientBuilder.newClient();
         WebTarget webTarget = client.target(String.format("http://%s%s/%s",
               _coordinator, 
               CoordinatorConstants.SERVICE_BASE,
               CoordinatorConstants.SERVICE_QUEUE_WORK))
               .queryParam(CoordinatorConstants.SERVICE_QUEUE_WORK_KEY, wItemString);
         Invocation.Builder invocationBuilder = webTarget
               .request(MediaType.APPLICATION_JSON);
         Response response = invocationBuilder.get();

         
         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         System.out.printf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals("")) {
            System.err.printf("got error while refreshing status: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }
         
         return true;
      }
      catch (ProcessingException e) {
         System.err.printf("unable to connect to %s: %s\n", _coordinator, e
               .getStackTrace().toString());
         return false;
      }
      catch (Exception e) {
         System.err.printf("exception: %s\n", e.getStackTrace().toString());
         return false;
      }
   }
}