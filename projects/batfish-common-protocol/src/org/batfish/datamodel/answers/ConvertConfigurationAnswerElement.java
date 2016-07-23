package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvertConfigurationAnswerElement implements AnswerElement,
      Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedMap<String, Warnings> _warnings;

   public ConvertConfigurationAnswerElement() {
      _warnings = new TreeMap<String, Warnings>();
   }

   public SortedMap<String, Warnings> getWarnings() {
      return _warnings;
   }

   public void setWarnings(SortedMap<String, Warnings> warnings) {
      _warnings = warnings;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      //TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
}
