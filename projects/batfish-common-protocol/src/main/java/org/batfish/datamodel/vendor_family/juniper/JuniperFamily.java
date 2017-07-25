package org.batfish.datamodel.vendor_family.juniper;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class JuniperFamily implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private String _rootAuthenticationEncryptedPassword;

  private SortedMap<String, TacplusServer> _tacplusServers;

  public JuniperFamily() {
    _tacplusServers = new TreeMap<>();
  }

  public String getRootAuthenticationEncryptedPassword() {
    return _rootAuthenticationEncryptedPassword;
  }

  public SortedMap<String, TacplusServer> getTacplusServers() {
    return _tacplusServers;
  }

  public void setRootAuthenticationEncryptedPassword(String rootAuthenticationEncryptedPassword) {
    _rootAuthenticationEncryptedPassword = rootAuthenticationEncryptedPassword;
  }

  public void setTacplusServers(SortedMap<String, TacplusServer> tacplusServers) {
    _tacplusServers = tacplusServers;
  }
}
