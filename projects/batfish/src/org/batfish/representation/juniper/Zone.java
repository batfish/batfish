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

   private final HostInboundSettings _hostInboundSettings;

   private final Map<Interface, HostInboundSettings> _interfaceHostInboundSettings;

   private final Map<String, FirewallFilter> _toZonePolicies;

   public Zone(String name, Map<String, AddressBook> globalAddressBooks) {
      super(name);
      _addressBook = new AddressBook(name, globalAddressBooks);
      _hostInboundSettings = new HostInboundSettings();
      _interfaceHostInboundSettings = new TreeMap<Interface, HostInboundSettings>();
      _toZonePolicies = new TreeMap<String, FirewallFilter>();
   }

   public AddressBook getAddressBook() {
      return _addressBook;
   }

   public HostInboundSettings getHostInboundSettings() {
      return _hostInboundSettings;
   }

   public Map<Interface, HostInboundSettings> getInterfaceHostInboundSettings() {
      return _interfaceHostInboundSettings;
   }

   public Map<String, FirewallFilter> getToZonePolicies() {
      return _toZonePolicies;
   }

}
