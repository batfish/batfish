package org.batfish.datamodel.packet_policy;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Interface;

/** {@link VrfExprVisitor} that provides the name of the specified VRF. */
@ParametersAreNonnullByDefault
public class VrfExprNameExtractor implements VrfExprVisitor<String> {
  @Nonnull private final Interface _ingressIface;

  public VrfExprNameExtractor(Interface ingressIface) {
    _ingressIface = ingressIface;
  }

  @Override
  public String visitLiteralVrfName(LiteralVrfName expr) {
    return expr.getVrfName();
  }

  @Override
  public String visitIngressInterfaceVrf(IngressInterfaceVrf expr) {
    return _ingressIface.getVrfName();
  }
}
