package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * ACI Application Profile (fvAp) semantic model.
 *
 * <p>An Application Profile is a logical container for EPGs that belong to the same application
 * tier or application.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationProfile implements Serializable {
  private final String _name;
  private String _tenant;
  private String _description;
  private List<String> _epgNames;

  public ApplicationProfile(String name) {
    _name = name;
    _epgNames = new ArrayList<>();
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

  public List<String> getEpgNames() {
    return _epgNames;
  }

  public void addEpg(String epgName) {
    _epgNames.add(epgName);
  }
}
