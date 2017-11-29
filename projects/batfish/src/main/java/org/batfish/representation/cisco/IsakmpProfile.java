package org.batfish.representation.cisco;

import org.batfish.common.util.DefinedStructure;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class IsakmpProfile extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private String _keyring;

  private Ip _localAddress;

  private Prefix _matchIdentity;

  public IsakmpProfile(String name, int definitionLine) {
    super(name, definitionLine);
  }

  public String getKeyring() {
    return _keyring;
  }

  public Ip getLocalAddress() {
    return _localAddress;
  }

  public Prefix getMatchIdentity() {
    return _matchIdentity;
  }

  public void setKeyring(String keyring) {
    _keyring = keyring;
  }

  public void setLocalAddress(Ip address) {
    _localAddress = address;
  }

  public void setMatchIdentity(Ip address, Ip mask) {
    _matchIdentity = new Prefix(address, mask);
  }
}
