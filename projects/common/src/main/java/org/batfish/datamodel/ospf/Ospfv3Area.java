package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Collection;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An OSPFv3 area configuration. */
@ParametersAreNonnullByDefault
public final class Ospfv3Area implements Serializable {

  public static final long DEFAULT_STUB_DEFAULT_METRIC =
      1L;

  public static final class Builder {
    private long _defaultMetric;
    private @Nonnull ImmutableSortedSet.Builder<String>
        _interfaces;
    private @Nullable Long _number;
    private boolean _stub;
    private boolean _suppressInterArea;

    private Builder() {
      _defaultMetric =
          DEFAULT_STUB_DEFAULT_METRIC;
      _interfaces =
          ImmutableSortedSet.naturalOrder();
    }

    public Ospfv3Area build() {
      checkArgument(
          _number != null,
          "Must set area number before building");

      checkArgument(
          !_suppressInterArea || _stub,
          "Cannot suppress inter-area routes in a non-stub area");

      checkArgument(
          _defaultMetric >= 0L
              && _defaultMetric <= 0xFFFFFFL,
          "Invalid OSPFv3 area default metric %s",
          _defaultMetric);

      return new Ospfv3Area(
          _number,
          _interfaces.build(),
          _stub,
          _suppressInterArea,
          _defaultMetric);
    }

    public Builder setNumber(long number) {
      _number = number;
      return this;
    }

    public Builder addInterface(
        String interfaceName) {
      _interfaces.add(interfaceName);
      return this;
    }

    public Builder addInterfaces(
        Collection<String> interfaceNames) {
      _interfaces.addAll(interfaceNames);
      return this;
    }

    public Builder setStub(boolean stub) {
      _stub = stub;
      return this;
    }

    /**
     * Suppress normal inter-area summaries into this stub area.
     *
     * <p>This corresponds to AOS-CX {@code area ... stub no-summary}.
     */
    public Builder setSuppressInterArea(
        boolean suppressInterArea) {
      _suppressInterArea = suppressInterArea;
      return this;
    }

    public Builder setDefaultMetric(
        long defaultMetric) {
      _defaultMetric = defaultMetric;
      return this;
    }
  }

  private static final String PROP_DEFAULT_METRIC =
      "defaultMetric";
  private static final String PROP_INTERFACES =
      "interfaces";
  private static final String PROP_NAME =
      "name";
  private static final String PROP_STUB =
      "stub";
  private static final String PROP_SUPPRESS_INTER_AREA =
      "suppressInterArea";

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static Ospfv3Area create(
      @JsonProperty(PROP_NAME)
          long number,
      @JsonProperty(PROP_INTERFACES)
          @Nullable SortedSet<String> interfaces,
      @JsonProperty(PROP_STUB)
          @Nullable Boolean stub,
      @JsonProperty(PROP_SUPPRESS_INTER_AREA)
          @Nullable Boolean suppressInterArea,
      @JsonProperty(PROP_DEFAULT_METRIC)
          @Nullable Long defaultMetric) {

    return new Ospfv3Area(
        number,
        firstNonNull(
            interfaces,
            ImmutableSortedSet.of()),
        firstNonNull(stub, false),
        firstNonNull(
            suppressInterArea,
            false),
        firstNonNull(
            defaultMetric,
            DEFAULT_STUB_DEFAULT_METRIC));
  }

  private Ospfv3Area(
      long areaNumber,
      Collection<String> interfaces,
      boolean stub,
      boolean suppressInterArea,
      long defaultMetric) {

    checkArgument(
        !suppressInterArea || stub,
        "Cannot suppress inter-area routes in a non-stub area");

    checkArgument(
        defaultMetric >= 0L
            && defaultMetric <= 0xFFFFFFL,
        "Invalid OSPFv3 area default metric %s",
        defaultMetric);

    _areaNumber = areaNumber;
    _interfaces =
        ImmutableSortedSet.copyOf(interfaces);
    _stub = stub;
    _suppressInterArea = suppressInterArea;
    _defaultMetric = defaultMetric;
  }

  @JsonProperty(PROP_NAME)
  public long getAreaNumber() {
    return _areaNumber;
  }

  @JsonProperty(PROP_INTERFACES)
  public @Nonnull SortedSet<String>
      getInterfaces() {
    return _interfaces;
  }

  @JsonProperty(PROP_STUB)
  public boolean getStub() {
    return _stub;
  }

  @JsonProperty(PROP_SUPPRESS_INTER_AREA)
  public boolean getSuppressInterArea() {
    return _suppressInterArea;
  }

  @JsonProperty(PROP_DEFAULT_METRIC)
  public long getDefaultMetric() {
    return _defaultMetric;
  }

  private final long _areaNumber;
  private final long _defaultMetric;
  private final @Nonnull SortedSet<String>
      _interfaces;
  private final boolean _stub;
  private final boolean _suppressInterArea;
}
