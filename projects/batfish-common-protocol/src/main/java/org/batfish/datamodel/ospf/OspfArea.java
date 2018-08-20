package org.batfish.datamodel.ospf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.NetworkFactory.NetworkFactoryBuilder;
import org.batfish.datamodel.Prefix;

public class OspfArea extends ComparableStructure<Long> implements Serializable {

  public static class Builder extends NetworkFactoryBuilder<OspfArea> {

    private NssaSettings _nssa;

    private Long _number;

    private OspfProcess _ospfProcess;

    private StubSettings _stub;

    private StubType _stubType;

    private boolean _injectDefaultRoute = DEFAULT_INJECT_DEFAULT_ROUTE;

    private int _metricOfDefaultRoute = DEFAULT_METRIC_OF_DEFAULT_ROUTE;

    Builder(NetworkFactory networkFactory) {
      super(networkFactory, OspfArea.class);
      _stubType = StubType.NONE;
    }

    @Override
    public OspfArea build() {
      long number = _number != null ? _number : generateLong();
      OspfArea ospfArea = new OspfArea(number);
      if (_ospfProcess != null) {
        _ospfProcess.getAreas().put(number, ospfArea);
      }
      ospfArea._nssa = _nssa;
      ospfArea._stub = _stub;
      ospfArea._stubType = _stubType;
      ospfArea._injectDefaultRoute = _injectDefaultRoute;
      ospfArea._metricOfDefaultRoute = _metricOfDefaultRoute;
      return ospfArea;
    }

    public Builder setNonStub() {
      _stubType = StubType.NONE;
      _nssa = null;
      _stub = null;
      return this;
    }

    public Builder setNssa(NssaSettings nssa) {
      _nssa = nssa;
      _stubType = StubType.NSSA;
      _stub = null;
      return this;
    }

    public Builder setNumber(Long number) {
      _number = number;
      return this;
    }

    public Builder setOspfProcess(OspfProcess ospfProcess) {
      _ospfProcess = ospfProcess;
      return this;
    }

    public Builder setStub(StubSettings stub) {
      _stub = stub;
      _stubType = StubType.STUB;
      _nssa = null;
      return this;
    }

    public Builder setStubType(StubType stubType) {
      _stubType = stubType;
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

  private static final String PROP_NSSA = "nssa";

  private static final String PROP_STUB = "stub";

  private static final String PROP_STUB_TYPE = "stubType";

  private static final String PROP_SUMMARIES = "summaries";

  private static final String PROP_SUMMARY_FILTER = "summaryFilter";

  private static final long serialVersionUID = 1L;

  public static Builder builder(NetworkFactory networkFactory) {
    return new Builder(networkFactory);
  }

  private boolean _injectDefaultRoute = DEFAULT_INJECT_DEFAULT_ROUTE;

  private SortedSet<String> _interfaces;

  private int _metricOfDefaultRoute = DEFAULT_METRIC_OF_DEFAULT_ROUTE;

  private NssaSettings _nssa;

  private StubSettings _stub;

  private StubType _stubType;

  private SortedMap<Prefix, OspfAreaSummary> _summaries;

  private String _summaryFilter;

  @JsonCreator
  public OspfArea(@JsonProperty(PROP_NAME) Long number) {
    super(number);
    _interfaces = new TreeSet<>();
    _stubType = StubType.NONE;
    _summaries = new TreeMap<>();
  }

  @JsonProperty(PROP_INJECT_DEFAULT_ROUTE)
  @JsonPropertyDescription("Whether the default route should be injected")
  public boolean getInjectDefaultRoute() {
    return _injectDefaultRoute;
  }

  @JsonProperty(PROP_INTERFACES)
  @JsonPropertyDescription("The interfaces assigned to this OSPF area")
  public SortedSet<String> getInterfaces() {
    return _interfaces;
  }

  @JsonProperty(PROP_METRIC_OF_DEFAULT_ROUTE)
  @JsonPropertyDescription("The metric to use for the injected default route")
  public int getMetricOfDefaultRoute() {
    return _metricOfDefaultRoute;
  }

  @JsonProperty(PROP_NSSA)
  public NssaSettings getNssa() {
    return _nssa;
  }

  @JsonProperty(PROP_STUB)
  public StubSettings getStub() {
    return _stub;
  }

  @JsonProperty(PROP_STUB_TYPE)
  public StubType getStubType() {
    return _stubType;
  }

  @JsonProperty(PROP_SUMMARIES)
  public SortedMap<Prefix, OspfAreaSummary> getSummaries() {
    return _summaries;
  }

  @JsonProperty(PROP_SUMMARY_FILTER)
  public String getSummaryFilter() {
    return _summaryFilter;
  }

  @JsonProperty(PROP_INJECT_DEFAULT_ROUTE)
  public void setInjectDefaultRoute(boolean injectDefaultRoute) {
    _injectDefaultRoute = injectDefaultRoute;
  }

  @JsonProperty(PROP_INTERFACES)
  public void setInterfaces(SortedSet<String> interfaces) {
    _interfaces = interfaces;
  }

  @JsonProperty(PROP_METRIC_OF_DEFAULT_ROUTE)
  public void setMetricOfDefaultRoute(int metricOfDefaultRoute) {
    _metricOfDefaultRoute = metricOfDefaultRoute;
  }

  @JsonProperty(PROP_NSSA)
  public void setNssa(NssaSettings nssa) {
    _nssa = nssa;
  }

  @JsonProperty(PROP_STUB)
  public void setStub(StubSettings stub) {
    _stub = stub;
  }

  @JsonProperty(PROP_STUB_TYPE)
  public void setStubType(StubType stubType) {
    _stubType = stubType;
  }

  @JsonProperty(PROP_SUMMARIES)
  public void setSummaries(SortedMap<Prefix, OspfAreaSummary> summaries) {
    _summaries = summaries;
  }

  @JsonProperty(PROP_SUMMARY_FILTER)
  public void setSummaryFilter(String summaryFilterName) {
    this._summaryFilter = summaryFilterName;
  }
}
