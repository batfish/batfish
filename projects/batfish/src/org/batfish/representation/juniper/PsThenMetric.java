package org.batfish.representation.juniper;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapSetMetricLine;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

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
   public void applyTo(List<Statement> statements,
         JuniperConfiguration juniperVendorConfiguration, Configuration c,
         Warnings warnings) {
      statements.add(new SetMetric(_metric));
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings) {
      clause.getSetLines().add(new PolicyMapSetMetricLine(_metric));
   }

   public int getMetric() {
      return _metric;
   }

}
