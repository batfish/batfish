package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.statement.SetOriginType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public class RouteMapSetOriginTypeLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private Integer _asNum;

   private OriginType _originType;

   public RouteMapSetOriginTypeLine(OriginType originType, Integer asNum) {
      _originType = originType;
      _asNum = asNum;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      statements.add(new SetOriginType(_originType));
   }

   public Integer getAsNum() {
      return _asNum;
   }

   public OriginType getOriginType() {
      return _originType;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.ORIGIN_TYPE;
   }

}
