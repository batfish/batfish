package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/** A {@link TransformationStep} that transforms the destination IP */
@ParametersAreNonnullByDefault
public final class AssignIpAddressFromPool implements TransformationStep, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_TRANSFORMATION_TYPE = "transformationType";
  private static final String PROP_IP_FIELD = "ipField";
  private static final String PROP_POOL = "pool";

  // deprecated
  private static final String PROP_POOL_END = "poolEnd";
  private static final String PROP_POOL_START = "poolStart";

  private final @Nonnull TransformationType _type;
  private final @Nonnull IpField _ipField;
  private final @Nonnull IpSpace _pool;

  public AssignIpAddressFromPool(TransformationType type, IpField ipField, IpSpace pool) {
    _type = type;
    _ipField = ipField;
    _pool = pool;
  }

  public AssignIpAddressFromPool(
      TransformationType type, IpField ipField, Ip poolStart, Ip poolEnd) {
    this(type, ipField, IpRange.range(poolStart, poolEnd));
  }

  @JsonCreator
  private static AssignIpAddressFromPool jsonCreator(
      @JsonProperty(PROP_TRANSFORMATION_TYPE) TransformationType type,
      @JsonProperty(PROP_IP_FIELD) IpField ipField,
      @JsonProperty(PROP_POOL) IpSpace pool,
      @JsonProperty(PROP_POOL_START) Ip poolStart,
      @JsonProperty(PROP_POOL_END) Ip poolEnd) {
    checkNotNull(type, PROP_TRANSFORMATION_TYPE + " cannot be null");
    checkNotNull(ipField, PROP_IP_FIELD + " cannot be null");
    if (pool == null) {
      checkNotNull(poolStart, PROP_POOL_START + " cannot be null");
      checkNotNull(poolEnd, PROP_POOL_END + " cannot be null");
      return new AssignIpAddressFromPool(type, ipField, poolStart, poolEnd);
    }
    return new AssignIpAddressFromPool(type, ipField, pool);
  }

  @Override
  public <T> T accept(TransformationStepVisitor<T> visitor) {
    return visitor.visitAssignIpAddressFromPool(this);
  }

  @JsonProperty(PROP_IP_FIELD)
  public @Nonnull IpField getIpField() {
    return _ipField;
  }

  @JsonProperty(PROP_POOL)
  public @Nonnull IpSpace getPool() {
    return _pool;
  }

  @Deprecated
  @JsonProperty(PROP_POOL_START)
  private Ip getPoolStart() {
    return Ip.ZERO;
  }

  @Deprecated
  @JsonProperty(PROP_POOL_END)
  private Ip getPoolEnd() {
    return Ip.ZERO;
  }

  @Override
  @JsonProperty(PROP_TRANSFORMATION_TYPE)
  public @Nonnull TransformationType getType() {
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
    return _type == that._type && _ipField == that._ipField && Objects.equals(_pool, that._pool);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _ipField, _pool);
  }
}
