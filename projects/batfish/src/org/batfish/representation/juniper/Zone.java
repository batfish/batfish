package org.batfish.representation.juniper;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.util.NamedStructure;

public final class Zone extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final AddressBook _addressBook;

   private final FirewallFilter _inboundFilter;

   private final Map<Interface, FirewallFilter> _inboundInterfaceFilters;

   private final Map<String, FirewallFilter> _toZonePolicies;

   public Zone(String name, Map<String, AddressBook> globalAddressBooks) {
      super(name);
      _addressBook = new AddressBook(name, globalAddressBooks);
      _inboundFilter = new FirewallFilter("~INBOUND_ZONE_FILTER~" + name,
            Family.INET);
      _inboundInterfaceFilters = new TreeMap<Interface, FirewallFilter>();
      _toZonePolicies = new TreeMap<String, FirewallFilter>();
   }

   public AddressBook getAddressBook() {
      return _addressBook;
   }

   public FirewallFilter getInboundFilter() {
      return _inboundFilter;
   }

   public Map<Interface, FirewallFilter> getInboundInterfaceFilters() {
      return _inboundInterfaceFilters;
   }

   public Map<String, FirewallFilter> getToZonePolicies() {
      return _toZonePolicies;
   }

}
