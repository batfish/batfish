package org.batfish.datamodel;

import java.io.Serializable;

public class AaaAuthenticationSettings implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private AaaAuthenticationLoginSettings _loginSettings;

   public AaaAuthenticationLoginSettings getLoginSettings() {
      return _loginSettings;
   }

   public void setLoginSettings(AaaAuthenticationLoginSettings loginSettings) {
      _loginSettings = loginSettings;
   }

}
