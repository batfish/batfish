package org.batfish.datamodel;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Line extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _transportInput;

   private String _transportOutput;

   private String _transportPreferred;

   @JsonCreator
   public Line(@JsonProperty(NAME_VAR) String name) {
      super(name);
   }

   public String getTransportInput() {
      return _transportInput;
   }

   public String getTransportOutput() {
      return _transportOutput;
   }

   public String getTransportPreferred() {
      return _transportPreferred;
   }

   public void setTransportInput(String transportInput) {
      _transportInput = transportInput;
   }

   public void setTransportOutput(String transportOutput) {
      _transportOutput = transportOutput;
   }

   public void setTransportPreferred(String transportPreferred) {
      _transportPreferred = transportPreferred;
   }

}
