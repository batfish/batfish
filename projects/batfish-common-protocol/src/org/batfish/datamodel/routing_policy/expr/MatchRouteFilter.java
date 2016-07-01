package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchRouteFilter extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private String _list;

   @JsonCreator
   public MatchRouteFilter() {
   }

   public MatchRouteFilter(String routeFilterName) {
      _list = routeFilterName;
   }

   public String getList() {
      return _list;
   }

   public void setList(String list) {
      _list = list;
   }

}
