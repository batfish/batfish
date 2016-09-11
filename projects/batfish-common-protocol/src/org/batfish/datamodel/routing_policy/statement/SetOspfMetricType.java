package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.OspfExternalRoute;
import org.batfish.datamodel.OspfMetricType;
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
   public Result execute(Environment environment,
         AbstractRouteBuilder<?> route) {
      Result result = new Result();
      OspfExternalRoute.Builder ospfExternalRoute = (OspfExternalRoute.Builder) route;
      ospfExternalRoute.setOspfMetricType(_metricType);
      return result;
   }

   public OspfMetricType getMetricType() {
      return _metricType;
   }

   public void setMetricType(OspfMetricType metricType) {
      _metricType = metricType;
   }

}
