package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Path attachment information linking EPGs to physical interfaces.
 *
 * <p>Path attachments (fvRsPathAtt) contain:
 *
 * <ul>
 *   <li>tDn: Target distinguished name identifying the physical interface
 *   <li>encap: VLAN encapsulation (e.g., "vlan-2717")
 *   <li>descr: Description of the attachment
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PathAttachment implements Serializable {
  private String _tdn;
  private String _podId;
  private String _nodeId;
  private String _nodeId2; // Secondary node ID for vPC
  private String _iface;
  private String _encap;
  private String _description;
  private String _epgName;
  private String _epgTenant;

  private String _addr;
  private String _mac;
  private String _mode;
  private String _interfaceType;

  public PathAttachment() {}

  public PathAttachment(String tdn) {
    _tdn = tdn;
    parseTdn(tdn);
  }

  public void setTargetDn(String tdn) {
    _tdn = tdn;
    parseTdn(tdn);
  }

  public String getTargetDn() {
    return _tdn;
  }

  private void parseTdn(String tdnValue) {
    if (tdnValue == null) {
      return;
    }
    extractPodId(tdnValue);
    extractNodeIds(tdnValue);
    extractInterface(tdnValue);
  }

  private void extractPodId(String tdnValue) {
    int podIdx = tdnValue.indexOf("/pod-");
    if (podIdx >= 0) {
      int podStart = podIdx + 5; // Skip "/pod-"
      int podEnd = tdnValue.indexOf('/', podStart);
      if (podEnd > podStart) {
        _podId = tdnValue.substring(podStart, podEnd);
      }
    }
  }

  private void extractNodeIds(String tdnValue) {
    int pathsIdx = tdnValue.indexOf("/paths-");
    int protpathsIdx = tdnValue.indexOf("/protpaths-");
    int nodeIdx = tdnValue.indexOf("/node-");

    if (protpathsIdx >= 0) {
      extractVpcNodeIds(tdnValue, protpathsIdx);
    } else if (pathsIdx >= 0) {
      extractSingleNodeId(tdnValue, pathsIdx);
    } else if (nodeIdx >= 0) {
      extractNodeRefId(tdnValue, nodeIdx);
    }
  }

  private void extractVpcNodeIds(String tdnValue, int protpathsIdx) {
    int nodeStart = protpathsIdx + 11; // Skip "/protpaths-"
    int slashIdx = tdnValue.indexOf('/', nodeStart);
    if (slashIdx > nodeStart) {
      String nodePair = tdnValue.substring(nodeStart, slashIdx);
      String[] nodes = nodePair.split("-");
      if (nodes.length >= 1) {
        _nodeId = nodes[0];
      }
      if (nodes.length >= 2) {
        _nodeId2 = nodes[1];
      }
    }
  }

  private void extractSingleNodeId(String tdnValue, int pathsIdx) {
    int nodeStart = pathsIdx + 7; // Skip "/paths-"
    int slashIdx = tdnValue.indexOf('/', nodeStart);
    if (slashIdx > nodeStart) {
      _nodeId = tdnValue.substring(nodeStart, slashIdx);
    }
  }

  private void extractNodeRefId(String tdnValue, int nodeIdx) {
    int nodeStart = nodeIdx + 6; // Skip "/node-"
    int slashIdx = tdnValue.indexOf('/', nodeStart);
    if (slashIdx > nodeStart) {
      _nodeId = tdnValue.substring(nodeStart, slashIdx);
    } else {
      // No trailing slash, take rest of string
      _nodeId = tdnValue.substring(nodeStart);
    }
  }

  private void extractInterface(String tdnValue) {
    // Extract interface name (pathep-[{interface}])
    int pathepIdx = tdnValue.indexOf("/pathep-[");
    if (pathepIdx >= 0) {
      int ifStart = pathepIdx + 9; // Skip "/pathep-["
      int ifEnd = tdnValue.indexOf(']', ifStart);
      if (ifEnd > ifStart) {
        _iface = normalizeInterfaceName(tdnValue.substring(ifStart, ifEnd));
      }
    }
  }

  private static @Nonnull String normalizeInterfaceName(@Nonnull String ifaceName) {
    String lower = ifaceName.toLowerCase();

    // Handle Ethernet interfaces: eth1/3 -> Ethernet1/3
    if (lower.startsWith("eth") && lower.matches("eth\\d+/\\d+.*")) {
      return "Ethernet" + ifaceName.substring(3);
    }

    // Handle port-channel: po1 -> port-channel1
    if (lower.startsWith("po") && lower.matches("po\\d+.*")) {
      return "port-channel" + ifaceName.substring(2);
    }

    // Handle Loopback: lo0 -> Loopback0
    if (lower.startsWith("lo") && lower.matches("lo\\d+.*")) {
      return "Loopback" + ifaceName.substring(2);
    }

    // Handle Vlan: vlan123 -> Vlan123
    if (lower.startsWith("vlan") && lower.matches("vlan\\d+.*")) {
      return "Vlan" + ifaceName.substring(4);
    }

    // Handle Tunnel: tun1 -> Tunnel1
    if (lower.startsWith("tun") && lower.matches("tun\\d+.*")) {
      return "Tunnel" + ifaceName.substring(3);
    }

    return ifaceName;
  }

  public String getTdn() {
    return getTargetDn();
  }

  public @Nullable String getPodId() {
    return _podId;
  }

  public @Nullable String getNodeId() {
    return _nodeId;
  }

  public void setNodeId(@Nullable String nodeId) {
    _nodeId = nodeId;
  }

  public @Nullable String getNodeId2() {
    return _nodeId2;
  }

  public boolean isVpc() {
    return _nodeId2 != null;
  }

  public @Nullable String getInterface() {
    return _iface;
  }

  public void setInterfaceName(@Nullable String iface) {
    _iface = iface;
  }

  public @Nullable String getEncap() {
    return _encap;
  }

  public void setEncapsulation(@Nullable String encap) {
    _encap = encap;
  }

  public void setEncap(@Nullable String encap) {
    setEncapsulation(encap);
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nullable String getEpgName() {
    return _epgName;
  }

  public void setEpgName(@Nullable String epgName) {
    _epgName = epgName;
  }

  public @Nullable String getEpgTenant() {
    return _epgTenant;
  }

  public void setEpgTenant(@Nullable String epgTenant) {
    _epgTenant = epgTenant;
  }

  public @Nullable String getAddress() {
    return _addr;
  }

  public void setAddress(@Nullable String addr) {
    _addr = addr;
  }

  public @Nullable String getMac() {
    return _mac;
  }

  public void setMac(@Nullable String mac) {
    _mac = mac;
  }

  public @Nullable String getMode() {
    return _mode;
  }

  public void setMode(@Nullable String mode) {
    _mode = mode;
  }

  public @Nullable String getInterfaceType() {
    return _interfaceType;
  }

  public void setInterfaceType(@Nullable String interfaceType) {
    _interfaceType = interfaceType;
  }
}
