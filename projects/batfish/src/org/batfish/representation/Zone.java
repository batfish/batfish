package org.batfish.representation;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;

public final class Zone extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final IpAccessList _fromHostFilter;

   private final IpAccessList _inboundFilter;

   private final Map<Interface, IpAccessList> _inboundInterfaceFilters;

   private final IpAccessList _toHostFilter;

   private final Map<String, IpAccessList> _toZonePolicies;

   public Zone(String name, IpAccessList inboundFilter,
         IpAccessList fromHostFilter, IpAccessList toHostFilter) {
      super(name);
      _inboundFilter = inboundFilter;
      _inboundInterfaceFilters = new TreeMap<Interface, IpAccessList>();
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

   public Map<Interface, IpAccessList> getInboundInterfaceFilters() {
      return _inboundInterfaceFilters;
   }

   public IpAccessList getToHostFilter() {
      return _toHostFilter;
   }

   public Map<String, IpAccessList> getToZonePolicies() {
      return _toZonePolicies;
   }

}
