package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Objects;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/**
 * A {@link TransformationStep} that transforms a port. For flows that don't have ports, (e.g. ICMP
 * flows), does nothing.
 */
public class AssignPortFromPool implements TransformationStep, Serializable {
  /**
   * The set of {@link IpProtocol IpProtocols} that can be transformed by {@link
   * AssignPortFromPool}.
   */
  public static final ImmutableSet<IpProtocol> PORT_TRANSFORMATION_PROTOCOLS =
      ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP);

  private static final String PROP_TRANSFORMATION_TYPE = "transformationType";
  private static final String PROP_PORT_FIELD = "portField";
  private static final String PROP_POOL_START = "poolStart";
  private static final String PROP_POOL_END = "poolEnd";

  private final TransformationType _type;
  private final PortField _portField;
  private final int _poolStart;
  private final int _poolEnd;

  public AssignPortFromPool(
      TransformationType type, PortField portField, int poolStart, int poolEnd) {
    _type = type;
    _portField = portField;
    _poolStart = poolStart;
    _poolEnd = poolEnd;
  }

  @JsonCreator
  private static AssignPortFromPool jsonCreator(
      @JsonProperty(PROP_TRANSFORMATION_TYPE) TransformationType type,
      @JsonProperty(PROP_PORT_FIELD) PortField portField,
      @JsonProperty(PROP_POOL_START) int poolStart,
      @JsonProperty(PROP_POOL_END) int poolEnd) {
    checkNotNull(type, PROP_TRANSFORMATION_TYPE + " cannot be null");
    checkNotNull(portField, PROP_PORT_FIELD + " cannot be null");
    return new AssignPortFromPool(type, portField, poolStart, poolEnd);
  }

  @Override
  public <T> T accept(TransformationStepVisitor<T> visitor) {
    return visitor.visitAssignPortFromPool(this);
  }

  @JsonProperty(PROP_PORT_FIELD)
  public PortField getPortField() {
    return _portField;
  }

  @JsonProperty(PROP_POOL_START)
  public int getPoolStart() {
    return _poolStart;
  }

  @JsonProperty(PROP_POOL_END)
  public int getPoolEnd() {
    return _poolEnd;
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
    if (!(o instanceof AssignPortFromPool)) {
      return false;
    }
    AssignPortFromPool that = (AssignPortFromPool) o;
    return _type == that._type
        && _portField == that._portField
        && _poolStart == that._poolStart
        && _poolEnd == that._poolEnd;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _portField, _poolStart, _poolEnd);
  }
}
