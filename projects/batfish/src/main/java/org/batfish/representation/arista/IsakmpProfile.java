package org.batfish.representation.arista;

import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;

public class IsakmpProfile implements Serializable {

  private String _keyring;

  @Nullable private Ip _localAddress;

  @Nonnull private String _localInterfaceName;

  private IpWildcard _matchIdentity;

  private final String _name;

  @Nullable private Ip _selfIdentity;

  public IsakmpProfile(String name) {
    _name = name;
    _localInterfaceName = UNSET_LOCAL_INTERFACE;
  }

  public String getKeyring() {
    return _keyring;
  }

  @Nullable
  public Ip getLocalAddress() {
    return _localAddress;
  }

  @Nonnull
  public String getLocalInterfaceName() {
    return _localInterfaceName;
  }

  public IpWildcard getMatchIdentity() {
    return _matchIdentity;
  }

  public String getName() {
    return _name;
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

  public void setLocalInterfaceName(@Nonnull String localInterfaceName) {
    _localInterfaceName = localInterfaceName;
  }

  public void setMatchIdentity(IpWildcard matchIdentity) {
    _matchIdentity = matchIdentity;
  }

  public void setSelfIdentity(Ip selfIdentity) {
    _selfIdentity = selfIdentity;
  }
}
