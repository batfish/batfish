package org.batfish.representation.juniper;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.representation.juniper.Nat.Type.SOURCE;
import static org.batfish.representation.juniper.Nat.Type.STATIC;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.Serializable;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
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
  public List<TransformationStep> toTransformationStep(Nat nat, Ip interfaceIp) {
    if (nat.getType() == STATIC) {
      throw new BatfishException("Juniper static nat is not supported");
    }

    TransformationType type = nat.getType() == SOURCE ? SOURCE_NAT : DEST_NAT;
    IpField ipField = nat.getType() == SOURCE ? IpField.SOURCE : IpField.DESTINATION;
    PortField portField = nat.getType() == SOURCE ? PortField.SOURCE : PortField.DESTINATION;

    ImmutableList.Builder<TransformationStep> builder = new Builder<>();
    builder.add(new AssignIpAddressFromPool(type, ipField, interfaceIp, interfaceIp));

    // PAT is enabled by default for interface source NAT
    if (type == SOURCE_NAT) {
      builder.add(
          new AssignPortFromPool(
              type, portField, nat.getDefaultFromPort(), nat.getDefaultToPort()));
    }

    return builder.build();
  }
}
