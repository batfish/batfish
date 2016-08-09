package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.OspfMetricType;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetOspfMetricType extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private OspfMetricType _metricType;

   @JsonCreator
   public SetOspfMetricType() {
   }

   public SetOspfMetricType(OspfMetricType metricType) {
      _metricType = metricType;
   }

   @Override
   public Result execute(Environment environment, Route route) {
      Result result = new Result();
      result.setReturn(false);
      return result;
   }

   public OspfMetricType getMetricType() {
      return _metricType;
   }

   public void setMetricType(OspfMetricType metricType) {
      _metricType = metricType;
   }

}
