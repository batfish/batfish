package org.batfish.representation.juniper;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapSetMetricLine;

public final class PsThenMetric extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _metric;

   public PsThenMetric(int metric) {
      _metric = metric;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      clause.getSetLines().add(new PolicyMapSetMetricLine(_metric));
   }

   public int getMetric() {
      return _metric;
   }

}
