package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.flow.TransformationStep.inferTypeFromIpField;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/** A {@link TransformationStep} that transforms the destination IP */
@ParametersAreNonnullByDefault
public final class AssignIpAddressFromPool implements TransformationStep, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private final TransformationType _type;
  private final IpField _ipField;
  private final Ip _poolStart;
  private final Ip _poolEnd;

  public AssignIpAddressFromPool(IpField ipField, Ip poolStart, Ip poolEnd) {
    this(inferTypeFromIpField(ipField), ipField, poolStart, poolEnd);
  }

  public AssignIpAddressFromPool(
      TransformationType type, IpField ipField, Ip poolStart, Ip poolEnd) {
    _type = type;
    _ipField = ipField;
    _poolStart = poolStart;
    _poolEnd = poolEnd;
  }

  @Override
  public <T> T accept(TransformationStepVisitor<T> visitor) {
    return visitor.visitAssignIpAddressFromPool(this);
  }

  public IpField getIpField() {
    return _ipField;
  }

  public Ip getPoolStart() {
    return _poolStart;
  }

  public Ip getPoolEnd() {
    return _poolEnd;
  }

  @Override
  public TransformationType getType() {
    return _type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AssignIpAddressFromPool)) {
      return false;
    }
    AssignIpAddressFromPool that = (AssignIpAddressFromPool) o;
    return _ipField == that._ipField
        && Objects.equals(_poolStart, that._poolStart)
        && Objects.equals(_poolEnd, that._poolEnd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ipField, _poolStart, _poolEnd);
  }
}
