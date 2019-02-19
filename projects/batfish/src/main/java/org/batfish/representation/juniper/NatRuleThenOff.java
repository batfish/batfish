package org.batfish.representation.juniper;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.representation.juniper.Nat.Type.SOURCE;
import static org.batfish.representation.juniper.Nat.Type.STATIC;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.TransformationStep;

/** Represents a {@code NatRuleThen} that turns off nat */
public enum NatRuleThenOff implements NatRuleThen {
  INSTANCE;

  @Override
  public List<TransformationStep> toTransformationSteps(Nat nat, Ip interfaceIp) {
    if (nat.getType() == STATIC) {
      throw new BatfishException("Juniper static nat is not supported");
    }

    TransformationType type = nat.getType() == SOURCE ? SOURCE_NAT : DEST_NAT;

    return ImmutableList.of(new Noop(type));
  }
}
