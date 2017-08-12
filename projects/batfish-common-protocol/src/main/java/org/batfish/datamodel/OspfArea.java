package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.common.util.ComparableStructure;

public class OspfArea extends ComparableStructure<Long> implements Serializable {

  private static final String PROP_INTERFACES = "interfaces";

  private static final long serialVersionUID = 1L;

  private static final String PROP_SUMMARIES = "summaries";

  private static final String PROP_SUMMARY_FILTER = "summaryFilter";

  private transient SortedSet<String> _interfaceNames;

  private SortedSet<Interface> _interfaces;

  private SortedMap<Prefix, Boolean> _summaries;

  private String _summaryFilter;

  @JsonCreator
  public OspfArea(@JsonProperty(PROP_NAME) Long number) {
    super(number);
    _interfaces = new TreeSet<>();
    _summaries = new TreeMap<>();
  }

  @JsonProperty(PROP_INTERFACES)
  @JsonPropertyDescription("The interfaces assigned to this OSPF area")
  public SortedSet<String> getInterfaceNames() {
    if (_interfaces != null && !_interfaces.isEmpty()) {
      return new TreeSet<>(_interfaces.stream().map(i -> i.getName()).collect(Collectors.toSet()));
    } else {
      return _interfaceNames;
    }
  }

  @JsonIgnore
  public SortedSet<Interface> getInterfaces() {
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
      _interfaces =
          new TreeSet<>(
              _interfaceNames
                  .stream()
                  .map(ifaceName -> owner.getInterfaces().get(ifaceName))
                  .collect(Collectors.toSet()));
    }
  }

  @JsonProperty(PROP_INTERFACES)
  public void setInterfaceNames(SortedSet<String> interfaceNames) {
    _interfaceNames = interfaceNames;
  }

  @JsonIgnore
  public void setInterfaces(SortedSet<Interface> interfaces) {
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
