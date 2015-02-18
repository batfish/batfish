package org.batfish.representation.juniper;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapSetLocalPreferenceLine;

public final class PsThenLocalPreference extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _localPreference;

   public PsThenLocalPreference(int localPreference) {
      _localPreference = localPreference;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      PolicyMapSetLocalPreferenceLine line = new PolicyMapSetLocalPreferenceLine(
            _localPreference);
      clause.getSetLines().add(line);
   }

   public int getLocalPreference() {
      return _localPreference;
   }

}
