package org.batfish.representation.juniper;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.util.ReferenceCountedStructure;
import org.batfish.datamodel.Prefix;

public class PrefixList extends ReferenceCountedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _definitionLine;

   private boolean _ipv6;

   private String _name;

   private Set<Prefix> _prefixes;

   public PrefixList(String name, int definitionLine) {
      _name = name;
      _definitionLine = definitionLine;
      _prefixes = new TreeSet<>();
   }

   public int getDefinitionLine() {
      return _definitionLine;
   }

   public boolean getIpv6() {
      return _ipv6;
   }

   public String getName() {
      return _name;
   }

   public Set<Prefix> getPrefixes() {
      return _prefixes;
   }

   public void setIpv6(boolean ipv6) {
      _ipv6 = ipv6;
   }

}
