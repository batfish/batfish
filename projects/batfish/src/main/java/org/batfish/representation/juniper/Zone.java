package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Zone implements Serializable {

  public enum AddressBookType {
    ATTACHED,
    INLINED,
    GLOBAL
  }

  private AddressBook _addressBook;

  private AddressBookType _addressBookType;

  private ConcreteFirewallFilter _fromHostFilter;

  private final Map<String, FirewallFilter> _fromZonePolicies;

  private final ConcreteFirewallFilter _inboundFilter;

  private final Map<String, ConcreteFirewallFilter> _inboundInterfaceFilters;

  private final List<String> _interfaces;

  private final String _name;

  private ConcreteFirewallFilter _toHostFilter;

  private final Map<String, ConcreteFirewallFilter> _toZonePolicies;

  private final Set<String> _screens;

  public Zone(String name, AddressBook globalAddressBook) {
    _name = name;
    _addressBook = globalAddressBook;
    _addressBookType = AddressBookType.GLOBAL;
    _inboundFilter = new ConcreteFirewallFilter("~INBOUND_ZONE_FILTER~" + name, Family.INET);
    _inboundInterfaceFilters = new TreeMap<>();
    _interfaces = new ArrayList<>();
    _fromZonePolicies = new TreeMap<>();
    _toZonePolicies = new TreeMap<>();
    _screens = new TreeSet<>();
  }

  public void attachAddressBook(AddressBook addressBook) {
    _addressBookType = AddressBookType.ATTACHED;
    _addressBook = addressBook;
  }

  public AddressBook initInlinedAddressBook(AddressBook globalAddressBook) {
    _addressBookType = AddressBookType.INLINED;
    _addressBook = new AddressBook(_name, globalAddressBook);
    return _addressBook;
  }

  public AddressBook getAddressBook() {
    return _addressBook;
  }

  public AddressBookType getAddressBookType() {
    return _addressBookType;
  }

  public ConcreteFirewallFilter getFromHostFilter() {
    return _fromHostFilter;
  }

  public Map<String, FirewallFilter> getFromZonePolicies() {
    return _fromZonePolicies;
  }

  public Set<String> getScreens() {
    return _screens;
  }

  public ConcreteFirewallFilter getInboundFilter() {
    return _inboundFilter;
  }

  public Map<String, ConcreteFirewallFilter> getInboundInterfaceFilters() {
    return _inboundInterfaceFilters;
  }

  public List<String> getInterfaces() {
    return _interfaces;
  }

  public String getName() {
    return _name;
  }

  public ConcreteFirewallFilter getToHostFilter() {
    return _toHostFilter;
  }

  public Map<String, ConcreteFirewallFilter> getToZonePolicies() {
    return _toZonePolicies;
  }

  public void setFromHostFilter(ConcreteFirewallFilter fromHostFilter) {
    _fromHostFilter = fromHostFilter;
  }

  public void setToHostFilter(ConcreteFirewallFilter toHostFilter) {
    _toHostFilter = toHostFilter;
  }
}
