package org.batfish.vendor.sonic.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the settings of a ACL_TABLE:
 * https://github.com/Azure/SONiC/wiki/Configuration#acl-and-mirroring
 */
public class AclTable implements Serializable {

  public enum Type {
    MIRROR,
    MIRRORV6,
    L3,
  }

  public enum Stage {
    INGRESS,
    EGRESS
  }

  public @Nonnull List<String> getPorts() {
    return _ports;
  }

  public @Nonnull Optional<Stage> getStage() {
    return Optional.ofNullable(_stage);
  }

  public @Nonnull Optional<Type> getType() {
    return Optional.ofNullable(_type);
  }

  private static final String PROP_PORTS = "ports";
  private static final String PROP_STAGE = "stage";
  private static final String PROP_TYPE = "type";

  private @Nonnull final List<String> _ports;
  private @Nullable final Stage _stage;
  private @Nullable final Type _type;

  @JsonCreator
  private @Nonnull static AclTable create(
      @Nullable @JsonProperty(PROP_PORTS) ImmutableList<String> ports,
      @Nullable @JsonProperty(PROP_STAGE) Stage stage,
      @Nullable @JsonProperty(PROP_TYPE) Type type) {
    return AclTable.builder().setPorts(ports).setStage(stage).setType(type).build();
  }

  private AclTable(List<String> ports, @Nullable Stage stage, @Nullable Type type) {
    _ports = ports;
    _stage = stage;
    _type = type;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AclTable)) {
      return false;
    }
    AclTable that = (AclTable) o;
    return _ports.equals(that._ports)
        && Objects.equals(_stage, that._stage)
        && Objects.equals(_type, that._type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ports, _stage, _type);
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("ports", _ports)
        .add("stage", _stage)
        .add("type", _type)
        .toString();
  }

  public @Nonnull static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ImmutableList<String> _ports;
    private Stage _stage;
    private Type _type;

    public @Nonnull Builder setPorts(@Nullable ImmutableList<String> ports) {
      this._ports = ports;
      return this;
    }

    public @Nonnull Builder setStage(@Nullable Stage stage) {
      this._stage = stage;
      return this;
    }

    public @Nonnull Builder setType(@Nullable Type type) {
      this._type = type;
      return this;
    }

    public @Nonnull AclTable build() {
      return new AclTable(firstNonNull(_ports, ImmutableList.of()), _stage, _type);
    }
  }
}
