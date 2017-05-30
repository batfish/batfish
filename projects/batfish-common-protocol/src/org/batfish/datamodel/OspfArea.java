package org.batfish.datamodel;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class OspfArea extends ComparableStructure<Long>
      implements Serializable {

   private static final String INTERFACES_VAR = "interfaces";

   private static final long serialVersionUID = 1L;

   private static final String SUMMARIES_VAR = "summaries";

   private static final String SUMMARY_FILTER_VAR = "summaryFilter";

   private transient SortedSet<String> _interfaceNames;

   private SortedSet<Interface> _interfaces;

   private SortedMap<Prefix, Boolean> _summaries;

   private String summaryFilter;

   @JsonCreator
   public OspfArea(@JsonProperty(NAME_VAR) Long number) {
      super(number);
      _interfaces = new TreeSet<>();
      _summaries = new TreeMap<>();
   }

   @JsonProperty(INTERFACES_VAR)
   @JsonPropertyDescription("The interfaces assigned to this OSPF area")
   public SortedSet<String> getInterfaceNames() {
      if (_interfaces != null && !_interfaces.isEmpty()) {
         return new TreeSet<>(_interfaces.stream().map(i -> i.getName())
               .collect(Collectors.toSet()));
      }
      else {
         return _interfaceNames;
      }
   }

   @JsonIgnore
   public SortedSet<Interface> getInterfaces() {
      return _interfaces;
   }

   @JsonProperty(SUMMARIES_VAR)
   public SortedMap<Prefix, Boolean> getSummaries() {
      return _summaries;
   }

   @JsonProperty(SUMMARY_FILTER_VAR)
   public String getSummaryFilter() {
      return summaryFilter;
   }

   public void resolveReferences(final Configuration owner) {
      if (_interfaceNames != null) {
         _interfaces = new TreeSet<>(_interfaceNames.stream()
               .map(ifaceName -> owner.getInterfaces().get(ifaceName))
               .collect(Collectors.toSet()));
      }
   }

   @JsonProperty(INTERFACES_VAR)
   public void setInterfaceNames(SortedSet<String> interfaceNames) {
      _interfaceNames = interfaceNames;
   }

   @JsonIgnore
   public void setInterfaces(SortedSet<Interface> interfaces) {
      _interfaces = interfaces;
   }

   @JsonProperty(SUMMARIES_VAR)
   public void setSummaries(SortedMap<Prefix, Boolean> summaries) {
      _summaries = summaries;
   }

   @JsonProperty(SUMMARY_FILTER_VAR)
   public void setSummaryFilter(String summaryFilterName) {
      this.summaryFilter = summaryFilterName;
   }

}
