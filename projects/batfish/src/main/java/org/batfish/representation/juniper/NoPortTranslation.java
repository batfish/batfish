package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Optional;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;

public class NoPortTranslation implements PortAddressTranslation, Serializable {

  public static final NoPortTranslation INSTANCE = new NoPortTranslation();

  private NoPortTranslation() {}

  @Override
  public Optional<TransformationStep> toTransformationStep(
      TransformationType type, PortField field) {
    return Optional.empty();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NoPortTranslation;
  }

  @Override
  public int hashCode() {
    return NoPortTranslation.class.getCanonicalName().hashCode();
  }
}
