package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.NetworkFactory.NetworkFactoryBuilder;

public class OspfArea extends ComparableStructure<Long> implements Serializable {

  public static class Builder extends NetworkFactoryBuilder<OspfArea> {

    private NssaSettings _nssa;

    private Long _number;

    private OspfProcess _ospfProcess;

    private StubSettings _stub;

    private StubType _stubType;

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

  private static final String PROP_INTERFACES = "interfaces";

  private static final String PROP_NSSA = "nssa";

  private static final String PROP_STUB = "stub";

  private static final String PROP_STUB_TYPE = "stubType";

  private static final String PROP_SUMMARIES = "summaries";

  private static final String PROP_SUMMARY_FILTER = "summaryFilter";

  private static final long serialVersionUID = 1L;

  private SortedSet<String> _interfaces;

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

  @JsonProperty(PROP_INTERFACES)
  @JsonPropertyDescription("The interfaces assigned to this OSPF area")
  public SortedSet<String> getInterfaces() {
    return _interfaces;
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

  @JsonProperty(PROP_INTERFACES)
  public void setInterfaces(SortedSet<String> interfaces) {
    _interfaces = interfaces;
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
