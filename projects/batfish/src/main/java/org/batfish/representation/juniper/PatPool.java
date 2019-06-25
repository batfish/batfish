package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;

public class PatPool implements PortAddressTranslation, Serializable {

  private final int _fromPort;

  private final int _toPort;

  public PatPool(int fromPort, int toPort) {
    _fromPort = fromPort;
    _toPort = toPort;
  }

  public Integer getFromPort() {
    return _fromPort;
  }

  public Integer getToPort() {
    return _toPort;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof PatPool)) {
      return false;
    }
    PatPool other = (PatPool) o;
    return _fromPort == other.getFromPort() && _toPort == other.getToPort();
  }

  @Override
  public int hashCode() {
    return Objects.hash(_fromPort, _toPort);
  }

  @Override
  public Optional<TransformationStep> toTransformationStep(
      TransformationType type, PortField field) {
    return Optional.of(new AssignPortFromPool(type, field, _fromPort, _toPort));
  }
}
