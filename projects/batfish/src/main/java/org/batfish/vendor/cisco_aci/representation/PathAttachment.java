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
  private String tdn;
  private String podId;
  private String nodeId;
  private String nodeId2; // Secondary node ID for vPC
  private String iface;
  private String encap;
  private String description;
  private String epgName;
  private String epgTenant;

  private String addr;
  private String mac;
  private String mode;
  private String interfaceType;

  public PathAttachment() {}

  public PathAttachment(String tdn) {
    this.tdn = tdn;
    parseTdn(tdn);
  }

  public void setTargetDn(String tdn) {
    this.tdn = tdn;
    parseTdn(tdn);
  }

  public String getTargetDn() {
    return tdn;
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
        podId = tdnValue.substring(podStart, podEnd);
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
        nodeId = nodes[0];
      }
      if (nodes.length >= 2) {
        nodeId2 = nodes[1];
      }
    }
  }

  private void extractSingleNodeId(String tdnValue, int pathsIdx) {
    int nodeStart = pathsIdx + 7; // Skip "/paths-"
    int slashIdx = tdnValue.indexOf('/', nodeStart);
    if (slashIdx > nodeStart) {
      nodeId = tdnValue.substring(nodeStart, slashIdx);
    }
  }

  private void extractNodeRefId(String tdnValue, int nodeIdx) {
    int nodeStart = nodeIdx + 6; // Skip "/node-"
    int slashIdx = tdnValue.indexOf('/', nodeStart);
    if (slashIdx > nodeStart) {
      nodeId = tdnValue.substring(nodeStart, slashIdx);
    } else {
      // No trailing slash, take rest of string
      nodeId = tdnValue.substring(nodeStart);
    }
  }

  private void extractInterface(String tdnValue) {
    // Extract interface name (pathep-[{interface}])
    int pathepIdx = tdnValue.indexOf("/pathep-[");
    if (pathepIdx >= 0) {
      int ifStart = pathepIdx + 9; // Skip "/pathep-["
      int ifEnd = tdnValue.indexOf(']', ifStart);
      if (ifEnd > ifStart) {
        iface = normalizeInterfaceName(tdnValue.substring(ifStart, ifEnd));
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
    return podId;
  }

  public @Nullable String getNodeId() {
    return nodeId;
  }

  public void setNodeId(@Nullable String nodeId) {
    this.nodeId = nodeId;
  }

  public @Nullable String getNodeId2() {
    return nodeId2;
  }

  public boolean isVpc() {
    return nodeId2 != null;
  }

  public @Nullable String getInterface() {
    return iface;
  }

  public void setInterfaceName(@Nullable String iface) {
    this.iface = iface;
  }

  public @Nullable String getEncap() {
    return encap;
  }

  public void setEncapsulation(@Nullable String encap) {
    this.encap = encap;
  }

  public void setEncap(@Nullable String encap) {
    setEncapsulation(encap);
  }

  public @Nullable String getDescription() {
    return description;
  }

  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public @Nullable String getEpgName() {
    return epgName;
  }

  public void setEpgName(@Nullable String epgName) {
    this.epgName = epgName;
  }

  public @Nullable String getEpgTenant() {
    return epgTenant;
  }

  public void setEpgTenant(@Nullable String epgTenant) {
    this.epgTenant = epgTenant;
  }

  public @Nullable String getAddress() {
    return addr;
  }

  public void setAddress(@Nullable String addr) {
    this.addr = addr;
  }

  public @Nullable String getMac() {
    return mac;
  }

  public void setMac(@Nullable String mac) {
    this.mac = mac;
  }

  public @Nullable String getMode() {
    return mode;
  }

  public void setMode(@Nullable String mode) {
    this.mode = mode;
  }

  public @Nullable String getInterfaceType() {
    return interfaceType;
  }

  public void setInterfaceType(@Nullable String interfaceType) {
    this.interfaceType = interfaceType;
  }
}
