package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_FROM;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_TO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a port range for a network acl entry */
@ParametersAreNonnullByDefault
final class PortRange implements Serializable {

  private final int _from;

  private final int _to;

  @JsonCreator
  private static PortRange create(
      @Nullable @JsonProperty(JSON_KEY_FROM) Integer fromPort,
      @Nullable @JsonProperty(JSON_KEY_TO) Integer toPort) {
    checkArgument(fromPort != null, "From port cannot be null in port range");
    checkArgument(toPort != null, "To port cannot be null in port range");
    return new PortRange(fromPort, toPort);
  }

  PortRange(int from, int to) {
    _from = from;
    _to = to;
  }

  int getFrom() {
    return _from;
  }

  int getTo() {
    return _to;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortRange)) {
      return false;
    }
    PortRange portRange = (PortRange) o;
    return _from == portRange._from && _to == portRange._to;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_from, _to);
  }
}
