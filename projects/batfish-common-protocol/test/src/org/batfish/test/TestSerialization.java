package org.batfish.test;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Prefix;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestSerialization {

   @Test
   public void test() throws JsonProcessingException, JSONException {
      ObjectMapper mapper = new ObjectMapper();
      BgpNeighbor neighbor = new BgpNeighbor((Prefix)null, null);
      String val = mapper.writeValueAsString(neighbor);
      JSONObject obj = new JSONObject(val);
      assert obj.length() == 1; // just id
   }

}
