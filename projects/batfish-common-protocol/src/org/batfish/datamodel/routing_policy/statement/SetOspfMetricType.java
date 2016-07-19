package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.OspfMetricType;

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

   public OspfMetricType getMetricType() {
      return _metricType;
   }

   public void setMetricType(OspfMetricType metricType) {
      _metricType = metricType;
   }

}
