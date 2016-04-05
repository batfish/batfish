package org.batfish.representation.juniper;

import java.util.LinkedHashMap;
import java.util.Map;

import org.batfish.util.ReferenceCountedStructure;

public final class PolicyStatement extends ReferenceCountedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private boolean _ipv6;

   private final String _name;

   private final PsTerm _singletonTerm;

   private final Map<String, PsTerm> _terms;

   public PolicyStatement(String name) {
      _name = name;
      String singletonTermName = "__" + _name + "__SINGLETON__";
      _singletonTerm = new PsTerm(singletonTermName);
      _terms = new LinkedHashMap<String, PsTerm>();
   }

   public boolean getIpv6() {
      return _ipv6;
   }

   public String getName() {
      return _name;
   }

   public PsTerm getSingletonTerm() {
      return _singletonTerm;
   }

   public Map<String, PsTerm> getTerms() {
      return _terms;
   }

   public void setIpv6(boolean ipv6) {
      _ipv6 = ipv6;
   }

}
