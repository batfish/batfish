package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapSetLocalPreferenceLine;
import org.batfish.main.Warnings;

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
   public void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings) {
      PolicyMapSetLocalPreferenceLine line = new PolicyMapSetLocalPreferenceLine(
            _localPreference);
      clause.getSetLines().add(line);
   }

   public int getLocalPreference() {
      return _localPreference;
   }

}
