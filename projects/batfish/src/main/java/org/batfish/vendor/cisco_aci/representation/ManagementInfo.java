package org.batfish.vendor.cisco_aci.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * ACI Management Information configuration.
 *
 * <p>Contains management IP address and gateway information for ACI fabric nodes.
 */
public class ManagementInfo implements Serializable {

  private String _address;
  private String _gateway;
  private String _gateway6;
  private String _address6;

  public @Nullable String getAddress() {
    return _address;
  }

  public void setAddress(@Nullable String address) {
    _address = address;
  }

  public @Nullable String getGateway() {
    return _gateway;
  }

  public void setGateway(@Nullable String gateway) {
    _gateway = gateway;
  }

  public @Nullable String getGateway6() {
    return _gateway6;
  }

  public void setGateway6(@Nullable String gateway6) {
    _gateway6 = gateway6;
  }

  public @Nullable String getAddress6() {
    return _address6;
  }

  public void setAddress6(@Nullable String address6) {
    _address6 = address6;
  }
}
