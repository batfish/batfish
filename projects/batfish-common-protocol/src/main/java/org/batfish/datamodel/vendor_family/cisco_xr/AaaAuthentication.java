package org.batfish.datamodel.vendor_family.cisco_xr;

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
