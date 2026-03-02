package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * L3Out path attachment configuration.
 *
 * <p>Represents the physical path attachment for an L3Out interface (l3extRsPathL3OutAtt). This
 * defines which physical interfaces and VLAN encapsulation are used for external connectivity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class L3OutPathAttachment implements Serializable {
  private String _targetDn;
  private String _encapsulation;
  private String _address;
  private String _mac;
  private String _mode;
  private String _interfaceType;
  private String _nodeId;
  private String _interfaceName;
  private String _description;

  /** The target distinguished name (tDn) pointing to the physical path. */
  public @Nullable String getTargetDn() {
    return _targetDn;
  }

  public void setTargetDn(String targetDn) {
    _targetDn = targetDn;
  }

  /** The VLAN encapsulation (e.g., "vlan-794"). */
  public @Nullable String getEncapsulation() {
    return _encapsulation;
  }

  public void setEncapsulation(String encapsulation) {
    _encapsulation = encapsulation;
  }

  /** The IP address on the interface. */
  public @Nullable String getAddress() {
    return _address;
  }

  public void setAddress(String address) {
    _address = address;
  }

  /** The MAC address of the SVI. */
  public @Nullable String getMac() {
    return _mac;
  }

  public void setMac(String mac) {
    _mac = mac;
  }

  /** The port mode (e.g., "regular", "native"). */
  public @Nullable String getMode() {
    return _mode;
  }

  public void setMode(String mode) {
    _mode = mode;
  }

  /** The interface instance type (e.g., "ext-svi", "sub-interface"). */
  public @Nullable String getInterfaceType() {
    return _interfaceType;
  }

  public void setInterfaceType(String interfaceType) {
    _interfaceType = interfaceType;
  }

  /** The node ID extracted from the tDn (for single-path) or null for VPC. */
  public @Nullable String getNodeId() {
    return _nodeId;
  }

  public void setNodeId(String nodeId) {
    _nodeId = nodeId;
  }

  /** The interface name extracted from the tDn. */
  public @Nullable String getInterfaceName() {
    return _interfaceName;
  }

  public void setInterfaceName(String interfaceName) {
    _interfaceName = interfaceName;
  }

  /** Description from the logical interface profile. */
  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
