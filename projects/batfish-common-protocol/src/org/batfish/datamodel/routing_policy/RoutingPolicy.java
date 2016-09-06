package org.batfish.datamodel.routing_policy;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.routing_policy.statement.Statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutingPolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String STATEMENTS_VAR = "statements";

   private List<Statement> _statements;

   @JsonCreator
   public RoutingPolicy(@JsonProperty(NAME_VAR) String name) {
      super(name);
      _statements = new ArrayList<>();
   }

   public Result call(Environment environment, AbstractRouteBuilder<?> route) {
      for (Statement statement : _statements) {
         Result result = statement.execute(environment, route);
         if (result.getExit()) {
            return result;
         }
         if (result.getReturn()) {
            result.setReturn(false);
            return result;
         }
      }
      Result result = new Result();
      result.setAction(environment.getDefaultAction());
      return result;
   }

   @JsonProperty(STATEMENTS_VAR)
   public List<Statement> getStatements() {
      return _statements;
   }

   public boolean process(AbstractRoute inputRoute,
         AbstractRouteBuilder<?> outputRoute) {
      Result result = call(new Environment(inputRoute), outputRoute);
      return result.getAction();
   }

   @JsonProperty(STATEMENTS_VAR)
   public void setStatements(List<Statement> statements) {
      _statements = statements;
   }

   public RoutingPolicy simplify() {
      List<Statement> simpleStatements = new ArrayList<>();
      for (Statement statement : _statements) {
         simpleStatements.addAll(statement.simplify());
      }
      RoutingPolicy simple = new RoutingPolicy(_key);
      simple.setStatements(simpleStatements);
      return simple;
   }

}
