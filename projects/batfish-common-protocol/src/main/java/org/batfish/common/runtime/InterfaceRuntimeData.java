package org.batfish.common.runtime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Represents runtime data for an interface */
public final class InterfaceRuntimeData {
  public static final class Builder {
    private @Nullable ConcreteInterfaceAddress _address;
    private @Nullable Double _bandwidth;
    private @Nullable Boolean _lineUp;
    private @Nullable Double _speed;

    private Builder() {}

    public InterfaceRuntimeData build() {
      return new InterfaceRuntimeData(_address, _bandwidth, _lineUp, _speed);
    }

    public Builder setAddress(@Nullable ConcreteInterfaceAddress address) {
      _address = address;
      return this;
    }

    public Builder setBandwidth(@Nullable Double bandwidth) {
      _bandwidth = bandwidth;
      return this;
    }

    public Builder setLineUp(@Nullable Boolean lineUp) {
      _lineUp = lineUp;
      return this;
    }

    public Builder setSpeed(@Nullable Double speed) {
      _speed = speed;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final InterfaceRuntimeData EMPTY_INTERFACE_RUNTIME_DATA = builder().build();

  private static final String PROP_ADDRESS = "address";
  private static final String PROP_BANDWIDTH = "bandwidth";
  private static final String PROP_LINE_UP = "lineUp";
  private static final String PROP_SPEED = "speed";

  // All properties should be nullable since a given interface may have runtime data for any/all
  private final @Nullable ConcreteInterfaceAddress _address;
  private final @Nullable Double _bandwidth;
  private final @Nullable Boolean _lineUp;
  private final @Nullable Double _speed;

  @JsonCreator
  @VisibleForTesting
  InterfaceRuntimeData(
      @JsonProperty(PROP_ADDRESS) @Nullable ConcreteInterfaceAddress address,
      @JsonProperty(PROP_BANDWIDTH) @Nullable Double bandwidth,
      @JsonProperty(PROP_LINE_UP) @Nullable Boolean lineUp,
      @JsonProperty(PROP_SPEED) @Nullable Double speed) {
    _address = address;
    _bandwidth = bandwidth;
    _lineUp = lineUp;
    _speed = speed;
  }

  @JsonProperty(PROP_ADDRESS)
  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  @JsonProperty(PROP_BANDWIDTH)
  public @Nullable Double getBandwidth() {
    return _bandwidth;
  }

  @JsonProperty(PROP_LINE_UP)
  public @Nullable Boolean getLineUp() {
    return _lineUp;
  }

  public Builder toBuilder() {
    return builder()
        .setAddress(_address)
        .setBandwidth(_bandwidth)
        .setLineUp(_lineUp)
        .setSpeed(_speed);
  }

  @JsonProperty(PROP_SPEED)
  public @Nullable Double getSpeed() {
    return _speed;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof InterfaceRuntimeData)) {
      return false;
    }
    InterfaceRuntimeData o = (InterfaceRuntimeData) obj;
    return Objects.equals(_address, o._address)
        && Objects.equals(_bandwidth, o._bandwidth)
        && Objects.equals(_lineUp, o._lineUp)
        && Objects.equals(_speed, o._speed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_address, _bandwidth, _lineUp, _speed);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add(PROP_ADDRESS, _address)
        .add(PROP_BANDWIDTH, _bandwidth)
        .add(PROP_LINE_UP, _lineUp)
        .add(PROP_SPEED, _speed)
        .toString();
  }
}
