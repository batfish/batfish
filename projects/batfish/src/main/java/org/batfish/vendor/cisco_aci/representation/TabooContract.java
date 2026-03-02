package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * ACI Taboo Contract (vzTaboo) semantic model.
 *
 * <p>A taboo contract defines traffic that is explicitly denied between EPGs, even when a regular
 * contract would permit the traffic.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TabooContract implements Serializable {
  private final String _name;
  private String _tenant;
  private String _description;
  private String _scope;
  private List<Contract.Subject> _subjects;

  public TabooContract(String name) {
    _name = name;
    _subjects = new ArrayList<>();
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

  public @Nullable String getScope() {
    return _scope;
  }

  public void setScope(String scope) {
    _scope = scope;
  }

  public List<Contract.Subject> getSubjects() {
    return _subjects;
  }

  public void setSubjects(List<Contract.Subject> subjects) {
    _subjects = new ArrayList<>(subjects);
  }
}
