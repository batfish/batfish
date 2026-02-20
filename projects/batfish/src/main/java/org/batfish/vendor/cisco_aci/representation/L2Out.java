package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class L2Out implements Serializable {
  private final String _name;
  private String _tenant;
  private String _description;
  private String _bridgeDomain;
  private String _encapsulation;

  public L2Out(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getTenant() {
    return _tenant;
  }

  public void setTenant(String tenant) {
    _tenant = tenant;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nullable String getBridgeDomain() {
    return _bridgeDomain;
  }

  public void setBridgeDomain(String bridgeDomain) {
    _bridgeDomain = bridgeDomain;
  }

  public @Nullable String getEncapsulation() {
    return _encapsulation;
  }

  public void setEncapsulation(String encapsulation) {
    _encapsulation = encapsulation;
  }
}
