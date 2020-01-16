package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** Represents an OSPF area configuration */
public class OspfArea implements Serializable {

  /** A builder for {@link OspfArea} */
  public static class Builder {

    private boolean _injectDefaultRoute = DEFAULT_INJECT_DEFAULT_ROUTE;
    @Nonnull private ImmutableSortedSet.Builder<String> _interfaces;
    @Nullable private NssaSettings _nssa;
    @Nullable private Long _number;
    @Nullable private Supplier<Long> _numberGenerator;
    private int _metricOfDefaultRoute = DEFAULT_METRIC_OF_DEFAULT_ROUTE;
    @Nullable private OspfProcess _ospfProcess;
    @Nullable private StubSettings _stub;
    @Nonnull private StubType _stubType;
    @Nonnull private ImmutableSortedMap.Builder<Prefix, OspfAreaSummary> _summaries;
    @Nullable private String _summaryFilter;

    private Builder(@Nullable Supplier<Long> numberGenerator) {
      _numberGenerator = numberGenerator;
      _interfaces = new ImmutableSortedSet.Builder<>(Ordering.natural());
      _stubType = StubType.NONE;
      _summaries = new ImmutableSortedMap.Builder<>(Ordering.natural());
    }

    public OspfArea build() {
      checkArgument(_number != null || _numberGenerator != null, "Must set number before building");
      long number = _number != null ? _number : _numberGenerator.get();
      OspfArea ospfArea =
          new OspfArea(
              number,
              _injectDefaultRoute,
              _interfaces.build(),
              _metricOfDefaultRoute,
              _nssa,
              _stub,
              _stubType,
              _summaries.build(),
              _summaryFilter);
      if (_ospfProcess != null) {
        _ospfProcess.addArea(ospfArea);
      }
      return ospfArea;
    }

    public Builder setInjectDefaultRoute(boolean injectDefaultRoute) {
      _injectDefaultRoute = injectDefaultRoute;
      return this;
    }

    public Builder addInterface(@Nonnull String interfaceName) {
      _interfaces.add(interfaceName);
      return this;
    }

    public Builder addInterfaces(@Nonnull Collection<String> interfaces) {
      _interfaces.addAll(interfaces);
      return this;
    }

    public Builder addSummary(@Nonnull Prefix prefix, @Nonnull OspfAreaSummary summary) {
      _summaries.put(prefix, summary);
      return this;
    }

    public Builder addSummaries(@Nonnull Map<Prefix, OspfAreaSummary> summaries) {
      _summaries.putAll(summaries);
      return this;
    }

    /** Replace all interfaces in the area */
    public Builder setInterfaces(@Nonnull Collection<String> interfaces) {
      _interfaces = new ImmutableSortedSet.Builder<>(Comparator.naturalOrder());
      _interfaces.addAll(interfaces);
      return this;
    }

    public Builder setMetricOfDefaultRoute(int metricOfDefaultRoute) {
      _metricOfDefaultRoute = metricOfDefaultRoute;
      return this;
    }

    /** Remove any stub settings, make this area not a Stub of any type */
    public Builder setNonStub() {
      _stubType = StubType.NONE;
      _nssa = null;
      _stub = null;
      return this;
    }

    /**
     * Set this area to be a not-so-stubby area with given settings, erase any other stub settings
     */
    public Builder setNssa(@Nullable NssaSettings nssa) {
      _stubType = StubType.NSSA;
      _nssa = nssa;
      _stub = null;
      return this;
    }

    /** Set the not-so-stubby settings only */
    public Builder setNssaSettings(@Nullable NssaSettings nssa) {
      _nssa = nssa;
      return this;
    }

    /** Set the area number */
    public Builder setNumber(long number) {
      _number = number;
      return this;
    }

    public Builder setOspfProcess(@Nonnull OspfProcess ospfProcess) {
      _ospfProcess = ospfProcess;
      return this;
    }

    /** Set this area to be a stub area with given settings, erase any NSSA settings */
    public Builder setStub(@Nullable StubSettings stub) {
      _stubType = StubType.STUB;
      _stub = stub;
      _nssa = null;
      return this;
    }

    /** Set Stub settings only */
    public Builder setStubSettings(@Nullable StubSettings stubSettings) {
      _stub = stubSettings;
      return this;
    }

    public Builder setStubType(@Nonnull StubType stubType) {
      _stubType = stubType;
      return this;
    }

    /** Replace the area summaries */
    public Builder setSummaries(@Nonnull Map<Prefix, OspfAreaSummary> summaries) {
      _summaries = new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());
      _summaries.putAll(summaries);
      return this;
    }

    public Builder setSummaryFilter(@Nullable String summaryFilter) {
      _summaryFilter = summaryFilter;
      return this;
    }
  }

  /*
   * Whether this OSPF Area should inject the default route. Some systems (like IOS) inject the
   * default route into OSPF by default, others don't (like JunOS). The default encodes the IOS
   * behavior; other implementations must override the default.
   */
  private static final boolean DEFAULT_INJECT_DEFAULT_ROUTE = true;

  /*
   * The metric of the default route injected by the OSPF Area (if it does inject the default
   * route). The default encodes the IOS behavior; other implementations must override the default.
   */
  private static final int DEFAULT_METRIC_OF_DEFAULT_ROUTE = 0;
  private static final String PROP_INJECT_DEFAULT_ROUTE = "injectDefaultRoute";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_METRIC_OF_DEFAULT_ROUTE = "metricOfDefaultRoute";
  private static final String PROP_NAME = "name";
  private static final String PROP_NSSA = "nssa";
  private static final String PROP_STUB = "stub";
  private static final String PROP_STUB_TYPE = "stubType";
  private static final String PROP_SUMMARIES = "summaries";
  private static final String PROP_SUMMARY_FILTER = "summaryFilter";

  public static Builder builder(Supplier<Long> nameGenerator) {
    return new Builder(nameGenerator);
  }

  public static Builder builder() {
    return new Builder(null);
  }

  private final long _areaNumber;
  private final boolean _injectDefaultRoute;
  // Not final because of the need to support addInterface
  @Nonnull private SortedSet<String> _interfaces;
  private final int _metricOfDefaultRoute;
  @Nullable private final NssaSettings _nssa;
  @Nullable private final StubSettings _stub;
  @Nonnull private final StubType _stubType;
  @Nonnull private final SortedMap<Prefix, OspfAreaSummary> _summaries;
  @Nullable private final String _summaryFilter;

  public OspfArea(
      long areaNumber,
      boolean injectDefaultRoute,
      @Nonnull SortedSet<String> interfaces,
      int metricOfDefaultRoute,
      @Nullable NssaSettings nssa,
      @Nullable StubSettings stub,
      @Nonnull StubType stubType,
      @Nonnull SortedMap<Prefix, OspfAreaSummary> summaries,
      @Nullable String summaryFilter) {
    _areaNumber = areaNumber;
    _injectDefaultRoute = injectDefaultRoute;
    _interfaces = ImmutableSortedSet.copyOf(interfaces);
    _metricOfDefaultRoute = metricOfDefaultRoute;
    _nssa = nssa;
    _stub = stub;
    _stubType = stubType;
    _summaries = ImmutableSortedMap.copyOf(summaries);
    _summaryFilter = summaryFilter;
  }

  @JsonCreator
  private static OspfArea create(
      @JsonProperty(PROP_NAME) long number,
      @JsonProperty(PROP_INJECT_DEFAULT_ROUTE) boolean injectDefaultRoute,
      @JsonProperty(PROP_INTERFACES) @Nullable SortedSet<String> interfaces,
      @JsonProperty(PROP_METRIC_OF_DEFAULT_ROUTE) int metricOfDefaultRoute,
      @JsonProperty(PROP_NSSA) @Nullable NssaSettings nssa,
      @JsonProperty(PROP_STUB) @Nullable StubSettings stub,
      @JsonProperty(PROP_STUB_TYPE) @Nullable StubType stubType,
      @JsonProperty(PROP_SUMMARIES) @Nullable SortedMap<Prefix, OspfAreaSummary> summaries,
      @JsonProperty(PROP_SUMMARY_FILTER) @Nullable String summaryFilter) {
    return new OspfArea(
        number,
        injectDefaultRoute,
        firstNonNull(interfaces, ImmutableSortedSet.of()),
        metricOfDefaultRoute,
        nssa,
        stub,
        firstNonNull(stubType, StubType.NONE),
        firstNonNull(summaries, ImmutableSortedMap.of()),
        summaryFilter);
  }

  /** Add a new interface into this area. */
  public void addInterface(String interfaceName) {
    _interfaces =
        new ImmutableSortedSet.Builder<String>(Ordering.natural())
            .addAll(_interfaces)
            .add(interfaceName)
            .build();
  }

  @JsonProperty(PROP_NAME)
  public long getAreaNumber() {
    return _areaNumber;
  }

  /** Whether the default route should be injected */
  @JsonProperty(PROP_INJECT_DEFAULT_ROUTE)
  public boolean getInjectDefaultRoute() {
    return _injectDefaultRoute;
  }

  /** The interfaces assigned to this OSPF area */
  @Nonnull
  @JsonProperty(PROP_INTERFACES)
  public SortedSet<String> getInterfaces() {
    return _interfaces;
  }

  /** The metric to use for the injected default route */
  @JsonProperty(PROP_METRIC_OF_DEFAULT_ROUTE)
  public int getMetricOfDefaultRoute() {
    return _metricOfDefaultRoute;
  }

  /** @deprecated Use {@link #getAreaNumber} */
  @Deprecated
  public long getName() {
    return getAreaNumber();
  }

  @Nullable
  @JsonProperty(PROP_NSSA)
  public NssaSettings getNssa() {
    return _nssa;
  }

  @Nullable
  @JsonProperty(PROP_STUB)
  public StubSettings getStub() {
    return _stub;
  }

  @Nonnull
  @JsonProperty(PROP_STUB_TYPE)
  public StubType getStubType() {
    return _stubType;
  }

  @Nonnull
  @JsonProperty(PROP_SUMMARIES)
  public SortedMap<Prefix, OspfAreaSummary> getSummaries() {
    return _summaries;
  }

  @Nullable
  @JsonProperty(PROP_SUMMARY_FILTER)
  public String getSummaryFilter() {
    return _summaryFilter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfArea)) {
      return false;
    }
    OspfArea ospfArea = (OspfArea) o;
    return _areaNumber == ospfArea._areaNumber
        && _injectDefaultRoute == ospfArea._injectDefaultRoute
        && _metricOfDefaultRoute == ospfArea._metricOfDefaultRoute
        && Objects.equals(_interfaces, ospfArea._interfaces)
        && Objects.equals(_nssa, ospfArea._nssa)
        && Objects.equals(_stub, ospfArea._stub)
        && _stubType == ospfArea._stubType
        && Objects.equals(_summaries, ospfArea._summaries)
        && Objects.equals(_summaryFilter, ospfArea._summaryFilter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _areaNumber,
        _injectDefaultRoute,
        _interfaces,
        _metricOfDefaultRoute,
        _nssa,
        _stub,
        _stubType,
        _summaries,
        _summaryFilter);
  }
}
