package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.Warnings;

public class ConvertConfigurationAnswerElement implements AnswerElement,
      Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Map<String, Warnings> _warnings;

   public ConvertConfigurationAnswerElement() {
      _warnings = new TreeMap<String, Warnings>();
   }

   public Map<String, Warnings> getWarnings() {
      return _warnings;
   }

   public void setWarnings(Map<String, Warnings> warnings) {
      _warnings = warnings;
   }

}
