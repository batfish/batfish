package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

/** Controller instance (ctrlrInst) in ACI fabric. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciCtrlrInst implements Serializable {
  private List<CtrlrInstChild> _children;

  public @Nullable List<CtrlrInstChild> getChildren() {
    return _children;
  }

  public void setChildren(@Nullable List<CtrlrInstChild> children) {
    _children = children;
  }

  /** Child elements of ctrlrInst. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CtrlrInstChild implements Serializable {
    private @Nullable AciFabricNodeIdentPol _fabricNodeIdentPol;

    @JsonProperty("fabricNodeIdentPol")
    public @Nullable AciFabricNodeIdentPol getFabricNodeIdentPol() {
      return _fabricNodeIdentPol;
    }

    @JsonProperty("fabricNodeIdentPol")
    public void setFabricNodeIdentPol(@Nullable AciFabricNodeIdentPol fabricNodeIdentPol) {
      _fabricNodeIdentPol = fabricNodeIdentPol;
    }
  }
}
