package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;

public class AaaAuthentication implements Serializable {

  private AaaAuthenticationLogin _login;

  public AaaAuthenticationLogin getLogin() {
    return _login;
  }

  public void setLogin(AaaAuthenticationLogin login) {
    _login = login;
  }
}
