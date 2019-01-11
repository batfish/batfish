package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  private static final String PROP_TRANSFORMATION_TYPE = "transformationType";
  private static final String PROP_IP_FIELD = "ipField";
  private static final String PROP_POOL_START = "poolStart";
  private static final String PROP_POOL_END = "poolEnd";

  private final TransformationType _type;
  private final IpField _ipField;
  private final Ip _poolStart;
  private final Ip _poolEnd;

  public AssignIpAddressFromPool(
      TransformationType type, IpField ipField, Ip poolStart, Ip poolEnd) {
    _type = type;
    _ipField = ipField;
    _poolStart = poolStart;
    _poolEnd = poolEnd;
  }

  @JsonCreator
  private static AssignIpAddressFromPool jsonCreator(
      @JsonProperty(PROP_TRANSFORMATION_TYPE) TransformationType type,
      @JsonProperty(PROP_IP_FIELD) IpField ipField,
      @JsonProperty(PROP_POOL_START) Ip poolStart,
      @JsonProperty(PROP_POOL_END) Ip poolEnd) {
    checkNotNull(type, PROP_TRANSFORMATION_TYPE + " cannot be null");
    checkNotNull(ipField, PROP_IP_FIELD + " cannot be null");
    checkNotNull(poolStart, PROP_POOL_START + " cannot be null");
    checkNotNull(poolEnd, PROP_POOL_END + " cannot be null");
    return new AssignIpAddressFromPool(type, ipField, poolStart, poolEnd);
  }

  @Override
  public <T> T accept(TransformationStepVisitor<T> visitor) {
    return visitor.visitAssignIpAddressFromPool(this);
  }

  @JsonProperty(PROP_IP_FIELD)
  public IpField getIpField() {
    return _ipField;
  }

  @JsonProperty(PROP_POOL_START)
  public Ip getPoolStart() {
    return _poolStart;
  }

  @JsonProperty(PROP_POOL_END)
  public Ip getPoolEnd() {
    return _poolEnd;
  }

  @Override
  @JsonProperty(PROP_TRANSFORMATION_TYPE)
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
    return _type == that._type
        && _ipField == that._ipField
        && Objects.equals(_poolStart, that._poolStart)
        && Objects.equals(_poolEnd, that._poolEnd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _ipField, _poolStart, _poolEnd);
  }
}
