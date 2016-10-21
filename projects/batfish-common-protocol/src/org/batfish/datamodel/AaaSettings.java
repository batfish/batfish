package org.batfish.datamodel;

import java.io.Serializable;

public class AaaSettings implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private AaaAuthenticationSettings _authenticationSettings;

   private boolean _newModel;

   public AaaAuthenticationSettings getAuthenticationSettings() {
      return _authenticationSettings;
   }

   public boolean getNewModel() {
      return _newModel;
   }

   public void setAuthenticationSettings(
         AaaAuthenticationSettings aaaAuthenticationSettings) {
      _authenticationSettings = new AaaAuthenticationSettings();
   }

   public void setNewModel(boolean newModel) {
      _newModel = newModel;
   }

}
