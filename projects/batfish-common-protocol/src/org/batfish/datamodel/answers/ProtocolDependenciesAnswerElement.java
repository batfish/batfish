package org.batfish.datamodel.answers;

import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProtocolDependenciesAnswerElement implements AnswerElement {

   private String _zipBase64;

   public String getZipBase64() {
      return _zipBase64;
   }

   public void setZipBase64(String zipBase64) {
      _zipBase64 = zipBase64;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      //TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

}
