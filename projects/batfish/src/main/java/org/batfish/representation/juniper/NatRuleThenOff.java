package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.TransformationStep;

/** Represents a {@code NatRuleThen} that turns off nat */
public enum NatRuleThenOff implements NatRuleThen {
  INSTANCE;

  @Override
  public List<TransformationStep> toTransformationStep(
      TransformationType type, IpField field, Map<String, NatPool> pools, Ip interfaceIp) {
    return ImmutableList.of(new Noop(type));
  }
}
