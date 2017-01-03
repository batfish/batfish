package org.batfish.datamodel;

import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("An IPV4 firewall zone")
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

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      }
      Zone other = (Zone) object;
      if (!this._fromHostFilter.equals(other._fromHostFilter)) {
         return false;
      }
      if (!this._inboundFilter.equals(other._inboundFilter)) {
         return false;
      }
      if (!this._inboundInterfaceFilters
            .equals(other._inboundInterfaceFilters)) {
         return false;
      }
      if (!this._toHostFilter.equals(other._toHostFilter)) {
         return false;
      }
      if (!this._toZonePolicies.equals(other._toZonePolicies)) {
         return false;
      }
      return true;
   }

   @JsonPropertyDescription("Filter applied against packets originating from an interface in this zone on this node")
   public IpAccessList getFromHostFilter() {
      return _fromHostFilter;
   }

   @JsonPropertyDescription("Filter applied against packets whose final destination is an interface in this zone that does not have its own inbound filter")
   public IpAccessList getInboundFilter() {
      return _inboundFilter;
   }

   @JsonPropertyDescription("Mapping of interfaces in this zone to their corresponding inbound filters: the filter applied against packets whose final destination is the interface whose name is the key in this mapping")
   public SortedMap<String, IpAccessList> getInboundInterfaceFilters() {
      return _inboundInterfaceFilters;
   }

   @JsonPropertyDescription("Filter applied against packets whose final destination is an interface in this zone. If this filter exists, it is applied IN ADDITION to the interface-specific or default inbound filter.")
   public IpAccessList getToHostFilter() {
      return _toHostFilter;
   }

   @JsonPropertyDescription("Maps names of destination zones to the corresponding filter applied against packets which are received on this zone and routed to the named zone")
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

   public boolean unorderedEqual(Object object) {
      if (this == object) {
         return true;
      }
      Zone other = (Zone) object;
      if (this.equals(other)) {
         return true;
      }
      if (!this._fromHostFilter.unorderedEqual(other._fromHostFilter)) {
         return false;
      }
      if (!this._inboundFilter.unorderedEqual(other._inboundFilter)) {
         return false;
      }
      if (unorderedEqualSortedMap(this._inboundInterfaceFilters,
            other._inboundInterfaceFilters)) {
         return false;
      }
      if (!this._toHostFilter.unorderedEqual(other._toHostFilter)) {
         return false;
      }
      if (unorderedEqualSortedMap(this._toZonePolicies,
            other._toZonePolicies)) {
         return false;
      }
      return true;
   }

   private boolean unorderedEqualSortedMap(SortedMap<String, IpAccessList> a,
         SortedMap<String, IpAccessList> b) {
      if (!a.keySet().equals(b.keySet())) {
         return false;
      }
      for (String s : a.keySet()) {
         if (!a.get(s).unorderedEqual(b.get(s))) {
            return false;
         }
      }
      return true;
   }

}
