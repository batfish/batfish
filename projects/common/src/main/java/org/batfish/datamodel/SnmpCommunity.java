package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;

public class SnmpCommunity extends ComparableStructure<String> {

  private String _accessList;
  private String _accessList6;

  /**
   * Indicates which IPv4 addresses are permitted to access this community.
   *
   * <p>Different from {@link #getAccessList()}, which just the name (if used) of a Cisco
   * access-list or a Juniper client-list or prefix-list [yes, neither is an access list], if
   * present, to indicate snmp clients. This can also be used to represent inline client lists, etc.
   */
  private @Nullable IpSpace _clientIps;

  private boolean _ro;
  private boolean _rw;

  @JsonCreator
  public SnmpCommunity(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }

  public String getAccessList() {
    return _accessList;
  }

  public void setAccessList(String accessList) {
    _accessList = accessList;
  }

  public String getAccessList6() {
    return _accessList6;
  }

  public void setAccessList6(String accessList6) {
    _accessList6 = accessList6;
  }

  public @Nullable IpSpace getClientIps() {
    return _clientIps;
  }

  public void setClientIps(@Nullable IpSpace clientIps) {
    _clientIps = clientIps;
  }

  public boolean getRo() {
    return _ro;
  }

  public void setRo(boolean ro) {
    _ro = ro;
  }

  public boolean getRw() {
    return _rw;
  }

  public void setRw(boolean rw) {
    _rw = rw;
  }
}
