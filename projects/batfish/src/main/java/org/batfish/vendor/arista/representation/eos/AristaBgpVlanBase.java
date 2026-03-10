package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public abstract class AristaBgpVlanBase implements Serializable {
  private @Nullable RouteDistinguisher _rd;
  private @Nullable ExtendedCommunity _rtImport;
  private @Nullable ExtendedCommunity _rtExport;

  public @Nullable RouteDistinguisher getRd() {
    return _rd;
  }

  public void setRd(@Nullable RouteDistinguisher rd) {
    _rd = rd;
  }

  public @Nullable ExtendedCommunity getRtImport() {
    return _rtImport;
  }

  public void setRtImport(@Nullable ExtendedCommunity rtImport) {
    _rtImport = rtImport;
  }

  public @Nullable ExtendedCommunity getRtExport() {
    return _rtExport;
  }

  public void setRtExport(@Nullable ExtendedCommunity rtExport) {
    _rtExport = rtExport;
  }
}
