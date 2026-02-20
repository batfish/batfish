package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * ACI Bridge Domain (fvBD) semantic model.
 *
 * <p>A bridge domain is a Layer 2 forwarding domain within a tenant. It contains subnets and can be
 * associated with a VRF.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BridgeDomain implements Serializable {
  private final String _name;
  private String _vrf;
  private String _tenant;
  private List<String> _subnets;
  private String _description;
  private String _encapsulation; // VLAN encapsulation (e.g., "vlan-100")

  public BridgeDomain(String name) {
    _name = name;
    _subnets = new ArrayList<>();
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }

  public @Nullable String getTenant() {
    return _tenant;
  }

  public void setTenant(String tenant) {
    _tenant = tenant;
  }

  public List<String> getSubnets() {
    return _subnets;
  }

  public void setSubnets(List<String> subnets) {
    _subnets = new ArrayList<>(subnets);
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nullable String getEncapsulation() {
    return _encapsulation;
  }

  public void setEncapsulation(String encapsulation) {
    _encapsulation = encapsulation;
  }
}
