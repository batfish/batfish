package org.batfish.datamodel.routing_policy;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.statement.Statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutingPolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String STATEMENTS_VAR = "statements";

   private transient Configuration _owner;

   private List<Statement> _statements;

   @JsonCreator
   public RoutingPolicy(@JsonProperty(NAME_VAR) String name) {
      super(name);
      _statements = new ArrayList<>();
   }

   public Result call(Environment environment) {
      for (Statement statement : _statements) {
         Result result = statement.execute(environment);
         if (result.getExit()) {
            return result;
         }
         if (result.getReturn()) {
            result.setReturn(false);
            return result;
         }
      }
      Result result = new Result();
      result.setBooleanValue(environment.getDefaultAction());
      return result;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      RoutingPolicy other = (RoutingPolicy) o;
      if (this.getStatements().equals(other.getStatements())){
         return true;
      }
      else {
         return false;
      }
   }
   
   @JsonIgnore
   public Configuration getOwner() {
      return _owner;
   }

   @JsonProperty(STATEMENTS_VAR)
   public List<Statement> getStatements() {
      return _statements;
   }

   public boolean process(AbstractRoute inputRoute,
         AbstractRouteBuilder<?> outputRoute, Ip peerAddress) {
      Result result = call(
            new Environment(_owner, inputRoute, outputRoute, peerAddress));
      return result.getBooleanValue();
   }

   public void setOwner(Configuration owner) {
      _owner = owner;
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
