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
    CTRLPLANE,
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

  public @Nonnull List<String> getServices() {
    return _services;
  }

  public @Nonnull Optional<Stage> getStage() {
    return Optional.ofNullable(_stage);
  }

  public @Nonnull Optional<Type> getType() {
    return Optional.ofNullable(_type);
  }

  /** Whether the port name refers to control plane */
  public boolean isControlPlanePort(String portName) {
    return portName.equalsIgnoreCase("CtrlPlane");
  }

  private static final String PROP_POLICY_DESC = "policy_desc";
  private static final String PROP_PORTS = "ports";
  private static final String PROP_SERVICES = "services";
  private static final String PROP_STAGE = "stage";
  private static final String PROP_TYPE = "type";

  private final @Nonnull List<String> _ports;
  private final @Nonnull List<String> _services;
  private final @Nullable Stage _stage;
  private final @Nullable Type _type;

  @SuppressWarnings("unused")
  @JsonCreator
  private static @Nonnull AclTable create(
      @JsonProperty(PROP_POLICY_DESC) @Nullable String policyDesc, // ignore
      @JsonProperty(PROP_PORTS) @Nullable List<String> ports,
      @JsonProperty(PROP_SERVICES) @Nullable List<String> services,
      @JsonProperty(PROP_STAGE) @Nullable Stage stage,
      @JsonProperty(PROP_TYPE) @Nullable Type type) {
    return AclTable.builder()
        .setPorts(ports)
        .setServices(services)
        .setStage(stage)
        .setType(type)
        .build();
  }

  private AclTable(
      List<String> ports, List<String> services, @Nullable Stage stage, @Nullable Type type) {
    _ports = ports;
    _services = services;
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
        && _services.equals(that._services)
        && Objects.equals(_stage, that._stage)
        && Objects.equals(_type, that._type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _ports,
        _services,
        _stage == null ? null : _stage.ordinal(),
        _type == null ? null : _type.ordinal());
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("ports", _ports)
        .add("services", _services)
        .add("stage", _stage)
        .add("type", _type)
        .toString();
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private List<String> _ports;
    private List<String> _services;
    private Stage _stage;
    private Type _type;

    public @Nonnull Builder setPorts(@Nullable List<String> ports) {
      this._ports = ports;
      return this;
    }

    public @Nonnull Builder setServices(@Nullable List<String> services) {
      this._services = services;
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
      return new AclTable(
          firstNonNull(_ports, ImmutableList.of()),
          firstNonNull(_services, ImmutableList.of()),
          _stage,
          _type);
    }
  }
}
