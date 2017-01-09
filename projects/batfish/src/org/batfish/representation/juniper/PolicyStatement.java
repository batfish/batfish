package org.batfish.representation.juniper;

import java.util.LinkedHashMap;
import java.util.Map;

import org.batfish.common.util.ReferenceCountedStructure;

public final class PolicyStatement extends ReferenceCountedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final PsTerm _defaultTerm;

   private final String _name;

   private final Map<String, PsTerm> _terms;

   public PolicyStatement(String name) {
      _name = name;
      String defaultTermName = "__" + _name + "__DEFAULT_TERM__";
      _defaultTerm = new PsTerm(defaultTermName);
      _terms = new LinkedHashMap<>();
   }

   public PsTerm getDefaultTerm() {
      return _defaultTerm;
   }

   public String getName() {
      return _name;
   }

   public Map<String, PsTerm> getTerms() {
      return _terms;
   }

}
