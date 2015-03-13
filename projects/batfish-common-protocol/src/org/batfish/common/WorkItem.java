package org.batfish.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class WorkItem {   
   public enum StatusCode { NEW, INPROGRESS, DONE }

   private UUID _id;
   private StatusCode _status;
   private HashMap<String, String> _requestParams;
   private HashMap<String, String> _responseParams;
   
   public WorkItem() {
      _id = UUID.randomUUID();
      _status = StatusCode.NEW;
      _requestParams = new HashMap<String, String>();
      _responseParams = new HashMap<String, String>();
   }
   
   public WorkItem(String jsonString) throws JSONException {

      JSONArray array = new JSONArray(jsonString);
      
      _id = UUID.fromString(array.get(0).toString());      
      _requestParams = new HashMap<String, String>();
      _responseParams = new HashMap<String, String>();

      JSONObject requestObject = new JSONObject(array.get(1).toString());      
      JSONObject responseObject = new JSONObject(array.get(2).toString());
      
      PopulateHashMap(_requestParams, requestObject);      
      PopulateHashMap(_responseParams, responseObject);      
   }
   
   private void PopulateHashMap(HashMap<String, String> map, JSONObject jsonObject) throws JSONException {

      Iterator<?> keys = jsonObject.keys();

      while( keys.hasNext() ) {
         String key = (String) keys.next();
         map.put(key,  jsonObject.getString(key));
      }
   }

   public void setId(String idString) {
      _id = UUID.fromString(idString);
   }

   public void addRequestParam(String key, String value) {
      _requestParams.put(key,  value);
   }
}