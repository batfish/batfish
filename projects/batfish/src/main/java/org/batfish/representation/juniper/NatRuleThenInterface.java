package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;

/** A {@link NatRule} that NATs using the interface's configured IP address. */
public final class NatRuleThenInterface implements NatRuleThen, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  public static final NatRuleThenInterface INSTANCE = new NatRuleThenInterface();

  private NatRuleThenInterface() {}

  @Override
  public List<TransformationStep> toTransformationStep(
      TransformationType type,
      IpField ipField,
      PortField portField,
      Map<String, NatPool> pools,
      Ip interfaceIp) {
    return ImmutableList.of(new AssignIpAddressFromPool(type, ipField, interfaceIp, interfaceIp));
  }
}
