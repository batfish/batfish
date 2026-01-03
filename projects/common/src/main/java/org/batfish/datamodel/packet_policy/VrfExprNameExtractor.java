package org.batfish.datamodel.packet_policy;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** {@link VrfExprVisitor} that provides the name of the specified VRF. */
@ParametersAreNonnullByDefault
public class VrfExprNameExtractor implements VrfExprVisitor<String> {
  private final @Nonnull String _ingressIfaceVrf;

  public VrfExprNameExtractor(String ingressIfaceVrf) {
    _ingressIfaceVrf = ingressIfaceVrf;
  }

  @Override
  public String visitLiteralVrfName(LiteralVrfName expr) {
    return expr.getVrfName();
  }

  @Override
  public String visitIngressInterfaceVrf(IngressInterfaceVrf expr) {
    return _ingressIfaceVrf;
  }
}
