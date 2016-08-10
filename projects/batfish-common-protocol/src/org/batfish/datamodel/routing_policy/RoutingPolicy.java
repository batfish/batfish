package org.batfish.datamodel.routing_policy;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Route;
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
      _statements = new ArrayList<Statement>();
   }

   public Result call(Environment environment, Route route) {
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

   public boolean permits(Route route) {
      Result result = call(new Environment(route), route);
      return result.getAction();
   }

   @JsonProperty(STATEMENTS_VAR)
   public void setStatements(List<Statement> statements) {
      _statements = statements;
   }

   public RoutingPolicy simplify() {
      List<Statement> simpleStatements = new ArrayList<Statement>();
      for (Statement statement : _statements) {
         simpleStatements.addAll(statement.simplify());
      }
      RoutingPolicy simple = new RoutingPolicy(_key);
      simple.setStatements(simpleStatements);
      return simple;
   }

}
