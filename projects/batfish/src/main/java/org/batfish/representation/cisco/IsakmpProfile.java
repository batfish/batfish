package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import org.batfish.common.util.DefinedStructure;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class IsakmpProfile extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private String _keyring;

  @Nullable private Ip _localAddress;

  @Nullable private String _localInterfaceName;

  private Prefix _matchIdentity;

  public IsakmpProfile(String name, int definitionLine) {
    super(name, definitionLine);
  }

  public String getKeyring() {
    return _keyring;
  }

  @Nullable
  public Ip getLocalAddress() {
    return _localAddress;
  }

  @Nullable
  public String getLocalInterfaceName() {
    return _localInterfaceName;
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

  public void setLocalInterfaceName(String localInterfaceName) {
    _localInterfaceName = localInterfaceName;
  }

  public void setMatchIdentity(Ip address, Ip mask) {
    _matchIdentity = new Prefix(address, mask);
  }
}
