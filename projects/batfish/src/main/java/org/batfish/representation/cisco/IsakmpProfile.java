package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class IsakmpProfile extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private String _keyring;

  @Nullable private Ip _localAddress;

  @Nullable private String _localInterfaceName;

  // TODO: change to IpWildcard
  private Prefix _matchIdentity;

  @Nullable private Ip _selfIdentity;

  public IsakmpProfile(String name) {
    super(name);
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

  public Ip getSelfIdentity() {
    return _selfIdentity;
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

  public void setSelfIdentity(Ip selfIdentity) {
    _selfIdentity = selfIdentity;
  }
}
