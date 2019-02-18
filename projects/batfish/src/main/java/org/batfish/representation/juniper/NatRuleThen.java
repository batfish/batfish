package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;

/** Represents the action part of a Juniper nat rule */
public interface NatRuleThen extends Serializable {
  List<TransformationStep> toTransformationStep(
      TransformationType type,
      IpField ipField,
      PortField portField,
      Map<String, NatPool> pools,
      Ip interfaceIp);
}
