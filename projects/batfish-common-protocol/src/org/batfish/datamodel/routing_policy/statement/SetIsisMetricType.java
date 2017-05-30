package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.IsisMetricType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetIsisMetricType extends Statement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IsisMetricType _metricType;

   @JsonCreator
   private SetIsisMetricType() {
   }

   public SetIsisMetricType(IsisMetricType metricType) {
      _metricType = metricType;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      SetIsisMetricType other = (SetIsisMetricType) obj;
      if (_metricType != other._metricType) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((_metricType == null) ? 0 : _metricType.hashCode());
      return result;
   }

   public void setMetricType(IsisMetricType metricType) {
      _metricType = metricType;
   }

}
