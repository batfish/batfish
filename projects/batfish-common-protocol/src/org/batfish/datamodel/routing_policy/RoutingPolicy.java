package org.batfish.datamodel.routing_policy;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.statement.Statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("A procedural routing policy used to transform and accept/reject IPV4/IPV6 routes")
public class RoutingPolicy extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String STATEMENTS_VAR = "statements";

   private Configuration _owner;

   private List<Statement> _statements;

   @JsonCreator
   private RoutingPolicy(@JsonProperty(NAME_VAR) String name) {
      super(name);
      _statements = new ArrayList<>();
   }

   public RoutingPolicy(String name, Configuration owner) {
      this(name);
      _owner = owner;
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
      result.setFallThrough(true);
      result.setBooleanValue(environment.getDefaultAction());
      return result;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      RoutingPolicy other = (RoutingPolicy) o;
      return _statements.equals(other._statements);
   }

   @JsonIgnore
   public Configuration getOwner() {
      return _owner;
   }

   @JsonProperty(STATEMENTS_VAR)
   @JsonPropertyDescription("The list of routing-policy statements to execute")
   public List<Statement> getStatements() {
      return _statements;
   }

   public boolean process(AbstractRoute inputRoute, AbstractRoute6 inputRoute6,
         AbstractRouteBuilder<?> outputRoute, Ip peerAddress, String vrf) {
      Environment environment = new Environment(_owner, vrf, inputRoute,
            inputRoute6, outputRoute, peerAddress);
      Result result = call(environment);
      return result.getBooleanValue();
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
      RoutingPolicy simple = new RoutingPolicy(_key, _owner);
      simple.setStatements(simpleStatements);
      return simple;
   }

}
