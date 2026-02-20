package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.annotation.Nullable;

/** ACI Contract Interface (vzCPIf) semantic model. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractInterface implements Serializable {
  private final String _name;
  private String _tenant;
  private String _description;

  public ContractInterface(String name) {
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
}
