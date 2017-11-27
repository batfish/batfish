package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
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

  private transient SortedSet<String> _interfaceNames;

  private SortedMap<String, Interface> _interfaces;

  private SortedMap<Prefix, Boolean> _summaries;

  private String _summaryFilter;

  @JsonCreator
  public OspfArea(@JsonProperty(PROP_NAME) Long number) {
    super(number);
    _interfaces = new TreeMap<>();
    _summaries = new TreeMap<>();
  }

  @JsonProperty(PROP_INTERFACES)
  @JsonPropertyDescription("The interfaces assigned to this OSPF area")
  public SortedSet<String> getInterfaceNames() {
    if (_interfaces != null && !_interfaces.isEmpty()) {
      return ImmutableSortedSet.copyOf(_interfaces.keySet());
    } else {
      return _interfaceNames;
    }
  }

  @JsonIgnore
  public SortedMap<String, Interface> getInterfaces() {
    return _interfaces;
  }

  @JsonProperty(PROP_SUMMARIES)
  public SortedMap<Prefix, Boolean> getSummaries() {
    return _summaries;
  }

  @JsonProperty(PROP_SUMMARY_FILTER)
  public String getSummaryFilter() {
    return _summaryFilter;
  }

  public void resolveReferences(final Configuration owner) {
    if (_interfaceNames != null) {
      ImmutableSortedMap.Builder<String, Interface> builder =
          new ImmutableSortedMap.Builder<>(String::compareTo);
      _interfaceNames
          .stream()
          .map(ifaceName -> owner.getInterfaces().get(ifaceName))
          .forEach(i -> builder.put(i.getName(), i));
      _interfaces = builder.build();
    }
  }

  @JsonProperty(PROP_INTERFACES)
  public void setInterfaceNames(SortedSet<String> interfaceNames) {
    _interfaceNames = interfaceNames;
  }

  @JsonIgnore
  public void setInterfaces(SortedMap<String, Interface> interfaces) {
    _interfaces = interfaces;
  }

  @JsonProperty(PROP_SUMMARIES)
  public void setSummaries(SortedMap<Prefix, Boolean> summaries) {
    _summaries = summaries;
  }

  @JsonProperty(PROP_SUMMARY_FILTER)
  public void setSummaryFilter(String summaryFilterName) {
    this._summaryFilter = summaryFilterName;
  }
}
