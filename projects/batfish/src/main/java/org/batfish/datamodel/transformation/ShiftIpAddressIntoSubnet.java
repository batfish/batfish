package org.batfish.datamodel.transformation;

import java.io.Serializable;
import java.util.Objects;
import org.batfish.datamodel.Prefix;

/** A {@link TransformationStep} that transforms the destination IP */
public final class ShiftIpAddressIntoSubnet implements TransformationStep, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private final IpField _ipField;
  private final Prefix _subnet;

  public ShiftIpAddressIntoSubnet(IpField ipField, Prefix subnet) {
    _ipField = ipField;
    _subnet = subnet;
  }

  @Override
  public <T> T accept(TransformationStepVisitor<T> visitor) {
    return visitor.visitShiftIpAddressIntoSubnet(this);
  }

  public IpField getIpField() {
    return _ipField;
  }

  public Prefix getSubnet() {
    return _subnet;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ShiftIpAddressIntoSubnet)) {
      return false;
    }
    ShiftIpAddressIntoSubnet that = (ShiftIpAddressIntoSubnet) o;
    return _ipField == that._ipField && Objects.equals(_subnet, that._subnet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ipField, _subnet);
  }
}
