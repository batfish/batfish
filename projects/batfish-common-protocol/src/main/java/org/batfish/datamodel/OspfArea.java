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

    private Long _number;

    private OspfProcess _ospfProcess;

    Builder(NetworkFactory networkFactory) {
      super(networkFactory, OspfArea.class);
    }

    @Override
    public OspfArea build() {
      long number = _number != null ? _number : generateLong();
      OspfArea ospfArea = new OspfArea(number);
      if (_ospfProcess != null) {
        _ospfProcess.getAreas().put(number, ospfArea);
      }
      return ospfArea;
    }

    public Builder setNumber(Long number) {
      _number = number;
      return this;
    }

    public Builder setOspfProcess(OspfProcess ospfProcess) {
      _ospfProcess = ospfProcess;
      return this;
    }
  }

  private static final String PROP_INTERFACES = "interfaces";

  private static final String PROP_SUMMARIES = "summaries";

  private static final String PROP_SUMMARY_FILTER = "summaryFilter";

  private static final long serialVersionUID = 1L;

  private SortedSet<String> _interfaces;

  private SortedMap<Prefix, OspfAreaSummary> _summaries;

  private String _summaryFilter;

  @JsonCreator
  public OspfArea(@JsonProperty(PROP_NAME) Long number) {
    super(number);
    _interfaces = new TreeSet<>();
    _summaries = new TreeMap<>();
  }

  @JsonProperty(PROP_INTERFACES)
  @JsonPropertyDescription("The interfaces assigned to this OSPF area")
  public SortedSet<String> getInterfaces() {
    return _interfaces;
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

  @JsonProperty(PROP_SUMMARIES)
  public void setSummaries(SortedMap<Prefix, OspfAreaSummary> summaries) {
    _summaries = summaries;
  }

  @JsonProperty(PROP_SUMMARY_FILTER)
  public void setSummaryFilter(String summaryFilterName) {
    this._summaryFilter = summaryFilterName;
  }
}
