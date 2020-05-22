package org.batfish.symbolic.state;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents state when the flow has matched a FIB lookup rule in a packet policy. At this point,
 * if the flow's dst IP is owned by the VRF where it arrived, it is accepted (represented by edges
 * to {@link InterfaceAccept}). Otherwise, it is forwarded based on the lookup VRF's FIB
 * (represented by edge to {@link PreOutVrf}).
 */
@ParametersAreNonnullByDefault
public final class PbrFibLookup implements StateExpr {
  @Nonnull private final String _hostname;
  @Nonnull private final String _ingressVrf;
  @Nonnull private final String _lookupVrf;

  public PbrFibLookup(String hostname, String ingressVrf, String lookupVrf) {
    _hostname = hostname;
    _ingressVrf = ingressVrf;
    _lookupVrf = lookupVrf;
  }

  @Nonnull
  public String getHostname() {
    return _hostname;
  }

  @Nonnull
  public String getIngressVrf() {
    return _ingressVrf;
  }

  @Nonnull
  public String getLookupVrf() {
    return _lookupVrf;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPbrFibLookup(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PbrFibLookup)) {
      return false;
    }
    PbrFibLookup o = (PbrFibLookup) obj;
    return _hostname.equals(o._hostname)
        && _ingressVrf.equals(o._ingressVrf)
        && _lookupVrf.equals(o._lookupVrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _ingressVrf, _lookupVrf);
  }
}
