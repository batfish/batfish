package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
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

    private @Nonnull ImmutableList.Builder<Ospfv3AreaRange>
        _ranges;

    private boolean _nssa;
    private @Nullable Long _number;
    private boolean _stub;
    private boolean _suppressInterArea;

    private Builder() {
      _defaultMetric =
          DEFAULT_STUB_DEFAULT_METRIC;

      _interfaces =
          ImmutableSortedSet.naturalOrder();

      _ranges =
          ImmutableList.builder();
    }

    public Ospfv3Area build() {
      checkArgument(
          _number != null,
          "Must set area number before building");

      checkArgument(
          !(_stub && _nssa),
          "An OSPFv3 area cannot be both stub and NSSA");

      checkArgument(
          !_suppressInterArea
              || _stub
              || _nssa,
          "Cannot suppress inter-area routes in a normal area");

      checkArgument(
          _defaultMetric >= 0L
              && _defaultMetric <= 0xFFFFFFL,
          "Invalid OSPFv3 area default metric %s",
          _defaultMetric);

      return new Ospfv3Area(
          _number,
          _interfaces.build(),
          _stub,
          _nssa,
          _suppressInterArea,
          _defaultMetric,
          _ranges.build());
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

    public Builder addRange(
        Ospfv3AreaRange range) {
      _ranges.add(range);
      return this;
    }

    public Builder addRanges(
        Collection<Ospfv3AreaRange> ranges) {
      _ranges.addAll(ranges);
      return this;
    }

    public Builder setStub(boolean stub) {
      _stub = stub;

      if (stub) {
        _nssa = false;
      }

      return this;
    }

    public Builder setNssa(boolean nssa) {
      _nssa = nssa;

      if (nssa) {
        _stub = false;
      }

      return this;
    }

    /**
     * Suppress ordinary inter-area summaries into this restricted area.
     *
     * <p>This represents {@code stub no-summary} and
     * {@code nssa no-summary}.
     */
    public Builder setSuppressInterArea(
        boolean suppressInterArea) {
      _suppressInterArea =
          suppressInterArea;
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

  private static final String PROP_NSSA =
      "nssa";

  private static final String PROP_RANGES =
      "ranges";

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
      @JsonProperty(PROP_NSSA)
          @Nullable Boolean nssa,
      @JsonProperty(PROP_SUPPRESS_INTER_AREA)
          @Nullable Boolean suppressInterArea,
      @JsonProperty(PROP_DEFAULT_METRIC)
          @Nullable Long defaultMetric,
      @JsonProperty(PROP_RANGES)
          @Nullable List<Ospfv3AreaRange> ranges) {

    return new Ospfv3Area(
        number,
        firstNonNull(
            interfaces,
            ImmutableSortedSet.of()),
        firstNonNull(
            stub,
            false),
        firstNonNull(
            nssa,
            false),
        firstNonNull(
            suppressInterArea,
            false),
        firstNonNull(
            defaultMetric,
            DEFAULT_STUB_DEFAULT_METRIC),
        firstNonNull(
            ranges,
            ImmutableList.of()));
  }

  private Ospfv3Area(
      long areaNumber,
      Collection<String> interfaces,
      boolean stub,
      boolean nssa,
      boolean suppressInterArea,
      long defaultMetric,
      Collection<Ospfv3AreaRange> ranges) {

    checkArgument(
        !(stub && nssa),
        "An OSPFv3 area cannot be both stub and NSSA");

    checkArgument(
        !suppressInterArea
            || stub
            || nssa,
        "Cannot suppress inter-area routes in a normal area");

    checkArgument(
        defaultMetric >= 0L
            && defaultMetric <= 0xFFFFFFL,
        "Invalid OSPFv3 area default metric %s",
        defaultMetric);

    _areaNumber = areaNumber;

    _interfaces =
        ImmutableSortedSet.copyOf(
            interfaces);

    _stub = stub;
    _nssa = nssa;

    _suppressInterArea =
        suppressInterArea;

    _defaultMetric =
        defaultMetric;

    _ranges =
        ImmutableList.copyOf(
            ranges);
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

  @JsonProperty(PROP_NSSA)
  public boolean getNssa() {
    return _nssa;
  }

  @JsonProperty(PROP_SUPPRESS_INTER_AREA)
  public boolean getSuppressInterArea() {
    return _suppressInterArea;
  }

  @JsonProperty(PROP_DEFAULT_METRIC)
  public long getDefaultMetric() {
    return _defaultMetric;
  }

  @JsonProperty(PROP_RANGES)
  public @Nonnull List<Ospfv3AreaRange>
      getRanges() {
    return _ranges;
  }

  private final long _areaNumber;
  private final long _defaultMetric;

  private final @Nonnull SortedSet<String>
      _interfaces;

  private final boolean _nssa;

  private final @Nonnull List<Ospfv3AreaRange>
      _ranges;

  private final boolean _stub;
  private final boolean _suppressInterArea;
}
