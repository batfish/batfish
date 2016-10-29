package org.batfish.datamodel.vendor_family.cisco;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Line extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Integer _execTimeoutMinutes;

   private Integer _execTimeoutSeconds;

   private String _inputAccessList;

   private String _loginAuthentication;

   private String _outputAccessList;

   private String _transportInput;

   private String _transportOutput;

   private String _transportPreferred;

   @JsonCreator
   public Line(@JsonProperty(NAME_VAR) String name) {
      super(name);
   }

   public Integer getExecTimeoutMinutes() {
      return _execTimeoutMinutes;
   }

   public Integer getExecTimeoutSeconds() {
      return _execTimeoutSeconds;
   }

   public String getInputAccessList() {
      return _inputAccessList;
   }

   public String getLoginAuthentication() {
      return _loginAuthentication;
   }

   public String getOutputAccessList() {
      return _outputAccessList;
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

   public void setExecTimeoutMinutes(Integer execTimeoutMinutes) {
      _execTimeoutMinutes = execTimeoutMinutes;
   }

   public void setExecTimeoutSeconds(Integer execTimeoutSeconds) {
      _execTimeoutSeconds = execTimeoutSeconds;
   }

   public void setInputAccessList(String inputAccessList) {
      _inputAccessList = inputAccessList;
   }

   public void setLoginAuthentication(String loginAuthentication) {
      _loginAuthentication = loginAuthentication;
   }

   public void setOutputAccessList(String outputAccessList) {
      _outputAccessList = outputAccessList;
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
