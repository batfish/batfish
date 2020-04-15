package org.batfish.representation.arista.eos;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public abstract class AristaBgpVlanBase implements Serializable {
  @Nullable private RouteDistinguisher _rd;
  @Nullable private ExtendedCommunity _rtImport;
  @Nullable private ExtendedCommunity _rtExport;

  @Nullable
  public RouteDistinguisher getRd() {
    return _rd;
  }

  public void setRd(@Nullable RouteDistinguisher rd) {
    _rd = rd;
  }

  @Nullable
  public ExtendedCommunity getRtImport() {
    return _rtImport;
  }

  public void setRtImport(@Nullable ExtendedCommunity rtImport) {
    _rtImport = rtImport;
  }

  @Nullable
  public ExtendedCommunity getRtExport() {
    return _rtExport;
  }

  public void setRtExport(@Nullable ExtendedCommunity rtExport) {
    _rtExport = rtExport;
  }
}
