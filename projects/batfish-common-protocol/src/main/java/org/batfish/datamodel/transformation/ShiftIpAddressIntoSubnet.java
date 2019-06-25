package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/**
 * A {@link TransformationStep} that transforms the an IP by shifting it into a subnet. For example,
 * the result of shifting {@code 1.2.3.4} into the subnet {@code 5.5.0.0/24} is {@code 5.5.0.4}. The
 * result of shifting {@code 1.2.3.4} into the subnet {@code 1.2.3.32/27} is {@code 1.2.3.36}.
 *
 * <p>All subnets are supported. Note that the /0 subnet is a noop -- in general, this
 * transformation is a noop whenever the transformed IP is already in the specified subnet.
 */
public final class ShiftIpAddressIntoSubnet implements TransformationStep, Serializable {

  private static final String PROP_TRANSFORMATION_TYPE = "transformationType";
  private static final String PROP_IP_FIELD = "ipField";
  private static final String PROP_SUBNET = "subnet";

  private final IpField _ipField;
  private final Prefix _subnet;
  private final TransformationType _type;

  public ShiftIpAddressIntoSubnet(TransformationType type, IpField ipField, Prefix subnet) {
    _ipField = ipField;
    _subnet = subnet;
    _type = type;
  }

  @JsonCreator
  private static ShiftIpAddressIntoSubnet jsonCreator(
      @JsonProperty(PROP_TRANSFORMATION_TYPE) TransformationType type,
      @JsonProperty(PROP_IP_FIELD) IpField ipField,
      @JsonProperty(PROP_SUBNET) Prefix subnet) {
    checkNotNull(type, PROP_TRANSFORMATION_TYPE + " cannot be null");
    checkNotNull(ipField, PROP_IP_FIELD + " cannot be null");
    checkNotNull(subnet, PROP_SUBNET + " cannot be null");
    return new ShiftIpAddressIntoSubnet(type, ipField, subnet);
  }

  @Override
  public <T> T accept(TransformationStepVisitor<T> visitor) {
    return visitor.visitShiftIpAddressIntoSubnet(this);
  }

  @JsonProperty(PROP_IP_FIELD)
  public IpField getIpField() {
    return _ipField;
  }

  @JsonProperty(PROP_SUBNET)
  public Prefix getSubnet() {
    return _subnet;
  }

  @JsonProperty(PROP_TRANSFORMATION_TYPE)
  public TransformationType getType() {
    return _type;
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
    return _type == that._type
        && _ipField == that._ipField
        && Objects.equals(_subnet, that._subnet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _ipField, _subnet);
  }
}
