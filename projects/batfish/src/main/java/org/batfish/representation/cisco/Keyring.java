package org.batfish.representation.cisco;

import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class Keyring extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  @Nullable private Ip _localAddress;

  @Nullable private String _localInterfaceName;

  private Ip _remoteAddress;

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

  public boolean match(Ip localAddress, Prefix matchIdentity) {
    return matchIdentity.containsIp(_remoteAddress)
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
}
