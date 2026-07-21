package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public abstract class AristaBgpVlanBase implements Serializable {
  private @Nullable RouteDistinguisher _rd;
  private final @Nonnull Set<ExtendedCommunity> _rtImports;
  private final @Nonnull Set<ExtendedCommunity> _rtExports;

  protected AristaBgpVlanBase() {
    _rtImports = new HashSet<>(0);
    _rtExports = new HashSet<>(0);
  }

  public @Nullable RouteDistinguisher getRd() {
    return _rd;
  }

  public void setRd(@Nullable RouteDistinguisher rd) {
    _rd = rd;
  }

  /** EVPN import route targets. A VLAN may declare multiple {@code route-target import} lines. */
  public @Nonnull Set<ExtendedCommunity> getRtImports() {
    return _rtImports;
  }

  public void addRtImport(ExtendedCommunity rtImport) {
    _rtImports.add(rtImport);
  }

  /** EVPN export route targets. A VLAN may declare multiple {@code route-target export} lines. */
  public @Nonnull Set<ExtendedCommunity> getRtExports() {
    return _rtExports;
  }

  public void addRtExport(ExtendedCommunity rtExport) {
    _rtExports.add(rtExport);
  }
}
