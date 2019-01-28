package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.TransformationStep;

/** A {@link NatRule} that NATs using the interface's configured IP address. */
public final class NatRuleThenInterface implements NatRuleThen, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  public static final NatRuleThenInterface INSTANCE = new NatRuleThenInterface();

  private NatRuleThenInterface() {}

  @Override
  public Optional<TransformationStep> toTransformationStep(
      TransformationType type, IpField field, Map<String, NatPool> pools, Ip interfaceIp) {
    return Optional.of(new AssignIpAddressFromPool(type, field, interfaceIp, interfaceIp));
  }
}
