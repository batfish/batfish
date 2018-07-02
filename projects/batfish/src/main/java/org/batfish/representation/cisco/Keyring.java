package org.batfish.representation.cisco;

import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public class Keyring extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  @Nullable private Ip _localAddress;

  @Nullable private String _localInterfaceName;

  // TODO: deprecate once old Ike Phase 1 data-model is phased out
  private Ip _remoteAddress;

  private IpWildcard _remoteIdentity;

  private String _key;

  public Keyring(String name) {
    super(name);
  }

  public String getKey() {
    return _key;
  }

  @Nullable
  public Ip getLocalAddress() {
    return _localAddress;
  }

  @Nullable
  public String getLocalInterfaceName() {
    return _localInterfaceName;
  }

  public Ip getRemoteAddress() {
    return _remoteAddress;
  }

  public IpWildcard getRemoteIdentity() {
    return _remoteIdentity;
  }

  public boolean match(Ip localAddress, Prefix matchIdentity) {
    IpWildcard candidateIpWildcard = new IpWildcard(matchIdentity);
    return _remoteIdentity.supersetOf(candidateIpWildcard)
        && (_localAddress == null || Objects.equals(localAddress, _localAddress));
  }

  public void setKey(String key) {
    _key = key;
  }

  public void setLocalAddress(Ip address) {
    _localAddress = address;
  }

  public void setLocalInterfaceName(String localInterfaceName) {
    _localInterfaceName = localInterfaceName;
  }

  public void setRemoteAddress(Ip address) {
    _remoteAddress = address;
  }

  public void setRemoteIdentity(IpWildcard remoteIdentity) {
    _remoteIdentity = remoteIdentity;
  }
}
