package org.batfish.datamodel.aaa;

import java.io.Serializable;

public class AaaAuthentication implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private AaaAuthenticationLogin _login;

   public AaaAuthenticationLogin getLogin() {
      return _login;
   }

   public void setLogin(AaaAuthenticationLogin login) {
      _login = login;
   }

}
