package org.batfish.question.jsonpath;

import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonPathQuery {

   private String _description;

   private String _path;

   private boolean _suffix;

   private boolean _summary;

   public String getDescription() {
      return _description;
   }

   public String getPath() {
      return _path;
   }

   public boolean getSuffix() {
      return _suffix;
   }

   public boolean getSummary() {
      return _summary;
   }

   public void setDescription(String description) {
      _description = description;
   }

   public void setPath(String path) {
      _path = path;
   }

   public void setSuffix(boolean suffix) {
      _suffix = suffix;
   }

   public void setSummary(boolean summary) {
      _summary = summary;
   }

   @Override
   public String toString() {
      BatfishObjectMapper mapper = new BatfishObjectMapper(false);
      try {
         return mapper.writeValueAsString(this);
      }
      catch (JsonProcessingException e) {
         throw new BatfishException("Could not map to JSON string", e);
      }
   }

}
