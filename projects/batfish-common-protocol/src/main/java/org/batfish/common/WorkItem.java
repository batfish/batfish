package org.batfish.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class WorkItem {
   public static WorkItem FromJsonString(String jsonString)
         throws JSONException {

      JSONArray array = new JSONArray(jsonString);

      UUID id = UUID.fromString(array.get(0).toString());

      String containerName = array.get(1).toString();
      String testrigName = array.get(2).toString();

      HashMap<String, String> requestParams = new HashMap<>();
      HashMap<String, String> responseParams = new HashMap<>();

      JSONObject requestObject = new JSONObject(array.get(3).toString());
      JSONObject responseObject = new JSONObject(array.get(4).toString());

      PopulateHashMap(requestParams, requestObject);
      PopulateHashMap(responseParams, responseObject);

      return new WorkItem(id, containerName, testrigName, requestParams,
            responseParams);
   }

   private static void PopulateHashMap(HashMap<String, String> map,
         JSONObject jsonObject) throws JSONException {

      Iterator<?> keys = jsonObject.keys();

      while (keys.hasNext()) {
         String key = (String) keys.next();
         map.put(key, jsonObject.getString(key));
      }
   }

   private String _containerName;
   private UUID _id;
   private HashMap<String, String> _requestParams;
   private HashMap<String, String> _responseParams;
   private String _testrigName;

   public WorkItem(String containerName, String testrigName) {
      _id = UUID.randomUUID();
      _containerName = containerName;
      _testrigName = testrigName;
      _requestParams = new HashMap<>();
      _responseParams = new HashMap<>();
   }

   public WorkItem(UUID id, String containerName, String testrigName,
         HashMap<String, String> reqParams, HashMap<String, String> resParams) {
      _id = id;
      _containerName = containerName;
      _testrigName = testrigName;
      _requestParams = reqParams;
      _responseParams = resParams;
   }

   public void addRequestParam(String key, String value) {
      _requestParams.put(key, value);
   }

   public String getContainerName() {
      return _containerName;
   }

   public UUID getId() {
      return _id;
   }

   public HashMap<String, String> getRequestParams() {
      return _requestParams;
   }

   public String getTestrigName() {
      return _testrigName;
   }

   public void setId(String idString) {
      _id = UUID.fromString(idString);
   }

   public String toJsonString() {
      JSONObject requestObject = new JSONObject(_requestParams);
      JSONObject responseObject = new JSONObject(_responseParams);
      JSONArray array = new JSONArray(Arrays.asList(_id, _containerName,
            _testrigName, requestObject.toString(), responseObject.toString()));
      return array.toString();
   }

   public JSONObject toTask() {
      return new JSONObject(_requestParams);
   }
}
