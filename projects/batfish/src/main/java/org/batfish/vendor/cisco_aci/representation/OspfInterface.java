package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * OSPF interface configuration for L3Out.
 *
 * <p>Defines OSPF interface-specific settings for an L3Out OSPF configuration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OspfInterface implements Serializable {
  private String _name;
  private String _description;
  private Integer _cost;
  private Integer _helloInterval;
  private Integer _deadInterval;
  private String _networkType;
  private Boolean _passive;

  public OspfInterface() {}

  public @Nullable String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nullable Integer getCost() {
    return _cost;
  }

  public void setCost(Integer cost) {
    _cost = cost;
  }

  public @Nullable Integer getHelloInterval() {
    return _helloInterval;
  }

  public void setHelloInterval(Integer helloInterval) {
    _helloInterval = helloInterval;
  }

  public @Nullable Integer getDeadInterval() {
    return _deadInterval;
  }

  public void setDeadInterval(Integer deadInterval) {
    _deadInterval = deadInterval;
  }

  public @Nullable String getNetworkType() {
    return _networkType;
  }

  public void setNetworkType(String networkType) {
    _networkType = networkType;
  }

  public @Nullable Boolean getPassive() {
    return _passive;
  }

  public void setPassive(Boolean passive) {
    _passive = passive;
  }
}
