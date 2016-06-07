package org.batfish.datamodel;

import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

public final class Zone extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final IpAccessList _fromHostFilter;

   private final IpAccessList _inboundFilter;

   private final SortedMap<String, IpAccessList> _inboundInterfaceFilters;

   private final IpAccessList _toHostFilter;

   private final SortedMap<String, IpAccessList> _toZonePolicies;

   public Zone(String name, IpAccessList inboundFilter,
         IpAccessList fromHostFilter, IpAccessList toHostFilter) {
      super(name);
      _inboundFilter = inboundFilter;
      _inboundInterfaceFilters = new TreeMap<String, IpAccessList>();
      _fromHostFilter = fromHostFilter;
      _toHostFilter = toHostFilter;
      _toZonePolicies = new TreeMap<String, IpAccessList>();
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

}
