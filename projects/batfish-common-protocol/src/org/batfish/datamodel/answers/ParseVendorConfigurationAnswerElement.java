package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.Warnings;

public class ParseVendorConfigurationAnswerElement implements AnswerElement,
      Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedMap<String, Warnings> _warnings;

   public ParseVendorConfigurationAnswerElement() {
      _warnings = new TreeMap<String, Warnings>();
   }

   public SortedMap<String, Warnings> getWarnings() {
      return _warnings;
   }

   public void setWarnings(SortedMap<String, Warnings> warnings) {
      _warnings = warnings;
   }

}
