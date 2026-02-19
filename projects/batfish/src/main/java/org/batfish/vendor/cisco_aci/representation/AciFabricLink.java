package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * ACI Fabric Link configuration.
 *
 * <p>Represents a physical link between two fabric nodes (spine-leaf, spine-spine, etc.). This data
 * comes from the optional fabric_links.json file, typically exported from APIC's fabricLink MOs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciFabricLink implements Serializable {

  private String _node1Id;
  private String _node1Interface;
  private String _node2Id;
  private String _node2Interface;
  private String _linkState; // "up", "down", etc.

  public AciFabricLink() {}

  public AciFabricLink(
      String node1Id, String node1Interface, String node2Id, String node2Interface) {
    _node1Id = node1Id;
    _node1Interface = node1Interface;
    _node2Id = node2Id;
    _node2Interface = node2Interface;
  }

  public @Nullable String getNode1Id() {
    return _node1Id;
  }

  public void setNode1Id(String node1Id) {
    _node1Id = node1Id;
  }

  public @Nullable String getNode1Interface() {
    return _node1Interface;
  }

  public void setNode1Interface(String node1Interface) {
    _node1Interface = node1Interface;
  }

  public @Nullable String getNode2Id() {
    return _node2Id;
  }

  public void setNode2Id(String node2Id) {
    _node2Id = node2Id;
  }

  public @Nullable String getNode2Interface() {
    return _node2Interface;
  }

  public void setNode2Interface(String node2Interface) {
    _node2Interface = node2Interface;
  }

  public @Nullable String getLinkState() {
    return _linkState;
  }

  public void setLinkState(String linkState) {
    _linkState = linkState;
  }
}
