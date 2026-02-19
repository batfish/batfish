package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * ACI Inter-Fabric Connection configuration.
 *
 * <p>Represents detected connections between ACI fabrics (e.g., DC1, DC2) via shared external
 * networks, BGP peers, or L3Out configurations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciInterFabricConnection implements Serializable {

  private String _fabric1;
  private String _fabric2;
  private String _connectionType; // "l3out", "bgp", "shared-external", "mpls"
  private String _l3OutName1;
  private String _l3OutName2;
  private List<String> _sharedSubnets;
  private List<String> _bgpPeers;
  private String _description;

  public AciInterFabricConnection() {
    _sharedSubnets = new ArrayList<>();
    _bgpPeers = new ArrayList<>();
  }

  public AciInterFabricConnection(
      String fabric1, String fabric2, String connectionType, String description) {
    this();
    _fabric1 = fabric1;
    _fabric2 = fabric2;
    _connectionType = connectionType;
    _description = description;
  }

  public @Nullable String getFabric1() {
    return _fabric1;
  }

  public void setFabric1(String fabric1) {
    _fabric1 = fabric1;
  }

  public @Nullable String getFabric2() {
    return _fabric2;
  }

  public void setFabric2(String fabric2) {
    _fabric2 = fabric2;
  }

  public @Nullable String getConnectionType() {
    return _connectionType;
  }

  public void setConnectionType(String connectionType) {
    _connectionType = connectionType;
  }

  public @Nullable String getL3OutName1() {
    return _l3OutName1;
  }

  public void setL3OutName1(String l3OutName1) {
    _l3OutName1 = l3OutName1;
  }

  public @Nullable String getL3OutName2() {
    return _l3OutName2;
  }

  public void setL3OutName2(String l3OutName2) {
    _l3OutName2 = l3OutName2;
  }

  public List<String> getSharedSubnets() {
    return _sharedSubnets;
  }

  public void setSharedSubnets(List<String> sharedSubnets) {
    _sharedSubnets = new ArrayList<>(sharedSubnets);
  }

  public void addSharedSubnet(String subnet) {
    _sharedSubnets.add(subnet);
  }

  public List<String> getBgpPeers() {
    return _bgpPeers;
  }

  public void setBgpPeers(List<String> bgpPeers) {
    _bgpPeers = new ArrayList<>(bgpPeers);
  }

  public void addBgpPeer(String bgpPeer) {
    _bgpPeers.add(bgpPeer);
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
