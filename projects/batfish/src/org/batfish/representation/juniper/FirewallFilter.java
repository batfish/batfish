package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FirewallFilter implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Family _family;

   private final String _name;

   private final Map<String, FwTerm> _terms;

   public FirewallFilter(String name, Family family) {
      _family = family;
      _name = name;
      _terms = new LinkedHashMap<String, FwTerm>();
   }

   public Family getFamily() {
      return _family;
   }

   public String getName() {
      return _name;
   }

   public Map<String, FwTerm> getTerms() {
      return _terms;
   }

}
