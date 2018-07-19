package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Zone implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final AddressBook _addressBook;

  private FirewallFilter _fromHostFilter;

  private final Map<String, FirewallFilter> _fromZonePolicies;

  private final FirewallFilter _inboundFilter;

  private final Map<String, FirewallFilter> _inboundInterfaceFilters;

  private final List<Interface> _interfaces;

  private final String _name;

  private FirewallFilter _toHostFilter;

  private final Map<String, FirewallFilter> _toZonePolicies;

  public Zone(String name, Map<String, AddressBook> globalAddressBooks) {
    _name = name;
    _addressBook = new AddressBook(name, globalAddressBooks);
    _inboundFilter = new FirewallFilter("~INBOUND_ZONE_FILTER~" + name, Family.INET);
    _inboundInterfaceFilters = new TreeMap<>();
    _interfaces = new ArrayList<>();
    _fromZonePolicies = new TreeMap<>();
    _toZonePolicies = new TreeMap<>();
  }

  public AddressBook getAddressBook() {
    return _addressBook;
  }

  public FirewallFilter getFromHostFilter() {
    return _fromHostFilter;
  }

  public Map<String, FirewallFilter> getFromZonePolicies() {
    return _fromZonePolicies;
  }

  public FirewallFilter getInboundFilter() {
    return _inboundFilter;
  }

  public Map<String, FirewallFilter> getInboundInterfaceFilters() {
    return _inboundInterfaceFilters;
  }

  public List<Interface> getInterfaces() {
    return _interfaces;
  }

  public String getName() {
    return _name;
  }

  public FirewallFilter getToHostFilter() {
    return _toHostFilter;
  }

  public Map<String, FirewallFilter> getToZonePolicies() {
    return _toZonePolicies;
  }

  public void setFromHostFilter(FirewallFilter fromHostFilter) {
    _fromHostFilter = fromHostFilter;
  }

  public void setToHostFilter(FirewallFilter toHostFilter) {
    _toHostFilter = toHostFilter;
  }
}
