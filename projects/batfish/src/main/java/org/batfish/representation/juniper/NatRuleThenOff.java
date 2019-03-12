package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.juniper.Nat.Type.DESTINATION;
import static org.batfish.representation.juniper.Nat.Type.SOURCE;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.TransformationStep;

/** Represents a {@code NatRuleThen} that turns off nat */
public enum NatRuleThenOff implements NatRuleThen {
  INSTANCE;

  @Override
  public List<TransformationStep> toTransformationSteps(
      JuniperConfiguration config, Nat nat, Ip interfaceIp, boolean reverse) {
    checkArgument(
        !reverse && (nat.getType() == SOURCE || nat.getType() == DESTINATION),
        "Interface actions can only be used in source nat and dest nat, and no reverse needed");

    TransformationType type = nat.getType().toTransformationType();

    return ImmutableList.of(new Noop(type));
  }
}
