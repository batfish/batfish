package org.batfish.representation;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PolicyMapClause implements Serializable {

   private static final long serialVersionUID = 1L;

   private PolicyMapAction _action;

   private Set<PolicyMapMatchLine> _matchList;

   private String _name;

   private Set<PolicyMapSetLine> _setList;

   public PolicyMapClause() {
      _matchList = new LinkedHashSet<PolicyMapMatchLine>();
      _setList = new LinkedHashSet<PolicyMapSetLine>();
   }

   public PolicyMapAction getAction() {
      return _action;
   }

   public Set<PolicyMapMatchLine> getMatchLines() {
      return _matchList;
   }

   public String getName() {
      return _name;
   }

   public Set<PolicyMapSetLine> getSetLines() {
      return _setList;
   }

   public void setAction(PolicyMapAction action) {
      _action = action;
   }

   public void setName(String name) {
      _name = name;
   }

}
