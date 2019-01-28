package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.TransformationStep;

/** Represents the action part of a Juniper nat rule */
public interface NatRuleThen extends Serializable {
  Optional<TransformationStep> toTransformationStep(
      TransformationType type, IpField field, Map<String, NatPool> pools, Ip interfaceIp);
}
