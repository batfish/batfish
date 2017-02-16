package org.batfish.datamodel.questions;

import java.util.Iterator;

import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class Question implements IQuestion {

   private static final String DIFF_VAR = "differential";

   private boolean _differential;

   public Question() {
      _differential = false;
   }

   @JsonIgnore
   public abstract boolean getDataPlane();

   @JsonProperty(DIFF_VAR)
   public boolean getDifferential() {
      return _differential;
   }

   @JsonIgnore
   public abstract String getName();

   @JsonIgnore
   public abstract boolean getTraffic();

   protected boolean isBaseParamKey(String paramKey) {
      switch (paramKey) {
      case DIFF_VAR:
         return true;
      default:
         return false;
      }
   }

   // by default, pretty printing is Json
   // override this function in derived classes to do something more meaningful
   public String prettyPrint() {
      ObjectMapper mapper = new BatfishObjectMapper();
      try {
         return mapper.writeValueAsString(this);
      }
      catch (JsonProcessingException e) {
         throw new BatfishException("Failed to pretty-print question", e);
      }
   }

   protected String prettyPrintBase() {
      String retString = "";
      // for brevity, print only if the values are non-default
      if (_differential) {
         retString += String.format("differential=%s", _differential);
      }
      if (retString == "") {
         return "";
      }
      else {
         return retString + " | ";
      }
   }

   public void setDifferential(boolean differential) {
      _differential = differential;
   }

   public void setJsonParameters(JSONObject parameters) {
      Iterator<?> paramKeys = parameters.keys();

      while (paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();

         if (!isBaseParamKey(paramKey)) {
            continue;
         }

         try {
            switch (paramKey) {
            case DIFF_VAR:
               setDifferential(parameters.getBoolean(paramKey));
               break;
            default:
               throw new BatfishException("Unhandled base param key in "
                     + getClass().getSimpleName() + ": " + paramKey);
            }
         }
         catch (JSONException e) {
            throw new BatfishException("JSONException in parameters", e);
         }
      }

   }

   @Override
   public String toFullJsonString() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      mapper.setSerializationInclusion(Include.ALWAYS);
      return mapper.writeValueAsString(this);
   }

   @Override
   public String toJsonString() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

}
