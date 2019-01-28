package org.batfish.representation.juniper;

import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.TransformationStep;

/** Represents a {@code NatRuleThen} that turns off nat */
public enum NatRuleThenOff implements NatRuleThen {
  INSTANCE;

  @Override
  public Optional<TransformationStep> toTransformationStep(
      TransformationType type, IpField field, Map<String, NatPool> pools, Ip interfaceIp) {
    return Optional.of(new Noop(type));
  }
}
