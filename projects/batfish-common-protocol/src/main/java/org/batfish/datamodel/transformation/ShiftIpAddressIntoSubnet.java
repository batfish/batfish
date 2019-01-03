package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Objects;
import org.batfish.datamodel.Prefix;

/**
 * A {@link TransformationStep} that transforms the an IP by shifting it into a subnet. For example,
 * the result of shifting {@code 1.2.3.4} into the subnet {@code 5.5.0.0/24} is {@code 5.5.0.4}. The
 * result of shifting {@code 1.2.3.4} into the subnet {@code 1.2.3.32/27} is {@code 1.2.3.36}.
 *
 * <p>/32 subnets are not supported -- they correspond to always setting the transformed IP address
 * to a signle value. For that, use {@link AssignIpAddressFromPool} with a single pool IP.
 *
 * <p>All subnets shorter than 32 bits are supported. Note that the /0 subnet is a noop -- in
 * general, this transformation is a noop whenever the transformed IP is already in the specified
 * subnet.
 */
public final class ShiftIpAddressIntoSubnet implements TransformationStep, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private final IpField _ipField;
  private final Prefix _subnet;

  public ShiftIpAddressIntoSubnet(IpField ipField, Prefix subnet) {
    checkArgument(
        subnet.getPrefixLength() < Prefix.MAX_PREFIX_LENGTH,
        "subnet prefix must be less than the maximum prefix length");
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
