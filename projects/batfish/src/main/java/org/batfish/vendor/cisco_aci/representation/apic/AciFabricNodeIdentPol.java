package org.batfish.vendor.cisco_aci.representation.apic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/** Fabric node identity policy (fabricNodeIdentPol) containing node identities. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciFabricNodeIdentPol implements Serializable {
  private List<AciFabricNodeIdentP> _children;

  public @Nullable List<AciFabricNodeIdentP> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<AciFabricNodeIdentP> children) {
    _children = children;
  }
}
