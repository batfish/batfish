package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.IsisMetricType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetIsisMetricType extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IsisMetricType _metricType;

   @JsonCreator
   public SetIsisMetricType() {
   }

   public SetIsisMetricType(IsisMetricType metricType) {
      _metricType = metricType;
   }

   @Override
   public Result execute(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public IsisMetricType getMetricType() {
      return _metricType;
   }

   public void setMetricType(IsisMetricType metricType) {
      _metricType = metricType;
   }

}
