package org.batfish.representation.cisco;

import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;

public class IsakmpProfile implements Serializable {

  private String _keyring;

  private @Nullable Ip _localAddress;

  private @Nonnull String _localInterfaceName;

  private IpWildcard _matchIdentity;

  private @Nullable String _matchVrf;

  private final String _name;

  private @Nullable String _vrf;

  private @Nullable Ip _selfIdentity;

  public IsakmpProfile(String name) {
    _name = name;
    _localInterfaceName = UNSET_LOCAL_INTERFACE;
  }

  public String getKeyring() {
    return _keyring;
  }

  public @Nullable Ip getLocalAddress() {
    return _localAddress;
  }

  public @Nonnull String getLocalInterfaceName() {
    return _localInterfaceName;
  }

  public IpWildcard getMatchIdentity() {
    return _matchIdentity;
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getMatchVrf() {
    return _matchVrf;
  }

  public void setMatchVrf(@Nullable String matchVrf) {
    _matchVrf = matchVrf;
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

  public void setVrf(@Nullable String vrf) {
    _vrf = vrf;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }
}
