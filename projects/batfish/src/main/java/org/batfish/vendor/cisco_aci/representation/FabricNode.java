package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;

/**
 * ACI Fabric Node semantic model.
 *
 * <p>A fabric node represents a physical or virtual switch in the ACI fabric. It contains interface
 * and connectivity information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FabricNode implements Serializable {
  private String _nodeId;
  private String _name;
  private String _role;
  private String _podId;
  private Map<String, FabricNodeInterface> _interfaces;
  private AciManagementInfo _managementInfo;

  public FabricNode() {
    _interfaces = new TreeMap<>();
  }

  public @Nullable String getNodeId() {
    return _nodeId;
  }

  public void setNodeId(String nodeId) {
    _nodeId = nodeId;
  }

  public @Nullable String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public @Nullable String getRole() {
    return _role;
  }

  public void setRole(String role) {
    _role = role;
  }

  public @Nullable String getPodId() {
    return _podId;
  }

  public void setPodId(String podId) {
    _podId = podId;
  }

  public Map<String, FabricNodeInterface> getInterfaces() {
    return _interfaces;
  }

  public void setInterfaces(Map<String, FabricNodeInterface> interfaces) {
    _interfaces = new TreeMap<>(interfaces);
  }

  public @Nullable AciManagementInfo getManagementInfo() {
    return _managementInfo;
  }

  public void setManagementInfo(@Nullable AciManagementInfo managementInfo) {
    _managementInfo = managementInfo;
  }
}
