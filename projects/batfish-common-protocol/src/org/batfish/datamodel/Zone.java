package org.batfish.datamodel;

import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Zone extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IpAccessList _fromHostFilter;

   private IpAccessList _inboundFilter;

   private SortedMap<String, IpAccessList> _inboundInterfaceFilters;

   private IpAccessList _toHostFilter;

   private SortedMap<String, IpAccessList> _toZonePolicies;

   @JsonCreator
   public Zone(@JsonProperty(NAME_VAR) String name) {
      super(name);
   }

   public Zone(String name, IpAccessList inboundFilter,
         IpAccessList fromHostFilter, IpAccessList toHostFilter) {
      super(name);
      _inboundFilter = inboundFilter;
      _inboundInterfaceFilters = new TreeMap<>();
      _fromHostFilter = fromHostFilter;
      _toHostFilter = toHostFilter;
      _toZonePolicies = new TreeMap<>();
   }

   public IpAccessList getFromHostFilter() {
      return _fromHostFilter;
   }

   public IpAccessList getInboundFilter() {
      return _inboundFilter;
   }

   public SortedMap<String, IpAccessList> getInboundInterfaceFilters() {
      return _inboundInterfaceFilters;
   }

   public IpAccessList getToHostFilter() {
      return _toHostFilter;
   }

   public SortedMap<String, IpAccessList> getToZonePolicies() {
      return _toZonePolicies;
   }

   public void setFromHostFilter(IpAccessList fromHostFilter) {
      _fromHostFilter = fromHostFilter;
   }

   public void setInboundFilter(IpAccessList inboundFilter) {
      _inboundFilter = inboundFilter;
   }

   public void setInboundInterfaceFilters(
         SortedMap<String, IpAccessList> inboundInterfaceFilters) {
      _inboundInterfaceFilters = inboundInterfaceFilters;
   }

   public void setToHostFilter(IpAccessList toHostFilter) {
      _toHostFilter = toHostFilter;
   }

   public void setToZonePolicies(
         SortedMap<String, IpAccessList> toZonePolicies) {
      _toZonePolicies = toZonePolicies;
   }

}
