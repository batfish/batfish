package org.batfish.representation.juniper;

import java.util.Optional;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;

public interface PortAddressTranslation {
  Optional<TransformationStep> toTransformationStep(TransformationType type, PortField field);
}
