package org.batfish.datamodel.answers;

import com.fasterxml.jackson.core.JsonProcessingException;

public class EnvironmentCreationAnswerElement implements AnswerElement {

   private String _newEnvironmentName;

   private String _oldEnvironmentName;

   public String getNewEnvironmentName() {
      return _newEnvironmentName;
   }

   public String getOldEnvironmentName() {
      return _oldEnvironmentName;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      return "New environment: '" + _newEnvironmentName
            + "' successfully created from old environment: '"
            + _oldEnvironmentName + "' with supplied parameters";
   }

   public void setNewEnvironmentName(String newEnvironmentName) {
      _newEnvironmentName = newEnvironmentName;
   }

   public void setOldEnvironmentName(String oldEnvironmentName) {
      _oldEnvironmentName = oldEnvironmentName;
   }

}
