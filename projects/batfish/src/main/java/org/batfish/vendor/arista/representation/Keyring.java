package org.batfish.vendor.arista.representation;

import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;

public class Keyring implements Serializable {

  private @Nullable Ip _localAddress;

  private @Nonnull String _localInterfaceName;

  private IpWildcard _remoteIdentity;

  private String _key;

  private final String _name;

  public Keyring(String name) {
    _name = name;
    _localInterfaceName = UNSET_LOCAL_INTERFACE;
  }

  public String getKey() {
    return _key;
  }

  public @Nullable Ip getLocalAddress() {
    return _localAddress;
  }

  public String getLocalInterfaceName() {
    return _localInterfaceName;
  }

  public String getName() {
    return _name;
  }

  public IpWildcard getRemoteIdentity() {
    return _remoteIdentity;
  }

  /**
   * Returns true if this {@link Keyring} can be used with the given localInterface and
   * matchIdentity
   *
   * @param localAddress {@link org.batfish.datamodel.Interface} {@link Ip} on which this {@link
   *     Keyring} is intended to be used
   * @param matchIdentity {@link IpWildcard} for the remote peers with which this {@link Keyring} is
   *     intended to be used
   * @return true if this {@link Keyring} can be used with the given localAddress and matchIdentity
   */
  public boolean match(Ip localAddress, IpWildcard matchIdentity) {
    return _remoteIdentity.supersetOf(matchIdentity)
        && (_localAddress == null || Objects.equals(localAddress, _localAddress));
  }

  public void setKey(String key) {
    _key = key;
  }

  public void setLocalAddress(Ip address) {
    _localAddress = address;
  }

  public void setLocalInterfaceName(@Nonnull String localInterfaceName) {
    _localInterfaceName = localInterfaceName;
  }

  public void setRemoteIdentity(IpWildcard remoteIdentity) {
    _remoteIdentity = remoteIdentity;
  }
}
