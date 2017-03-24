package org.batfish.representation.cisco;

import java.util.LinkedHashSet;
import java.util.Set;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

public class NamedBgpPeerGroup extends BgpPeerGroup {

   private static final long serialVersionUID = 1L;

   private final int _definitionLine;

   private String _name;

   private Set<Ip> _neighborIpAddresses;

   private Set<Prefix> _neighborIpPrefixes;

   private Set<Ip6> _neighborIpv6Addresses;

   private Set<Prefix6> _neighborIpv6Prefixes;

   public NamedBgpPeerGroup(String name, int definitionLine) {
      _name = name;
      _definitionLine = definitionLine;
      _neighborIpAddresses = new LinkedHashSet<>();
      _neighborIpPrefixes = new LinkedHashSet<>();
      _neighborIpv6Addresses = new LinkedHashSet<>();
      _neighborIpv6Prefixes = new LinkedHashSet<>();
   }

   public void addNeighborIpAddress(Ip address) {
      _neighborIpAddresses.add(address);
   }

   public void addNeighborIpPrefix(Prefix prefix) {
      _neighborIpPrefixes.add(prefix);
   }

   public void addNeighborIpv6Address(Ip6 address) {
      _neighborIpv6Addresses.add(address);
   }

   public void addNeighborIpv6Prefix(Prefix6 prefix6) {
      _neighborIpv6Prefixes.add(prefix6);
   }

   public int getDefinitionLine() {
      return _definitionLine;
   }

   @Override
   public String getName() {
      return _name;
   }

}
