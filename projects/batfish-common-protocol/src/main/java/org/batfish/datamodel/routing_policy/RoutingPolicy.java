package org.batfish.datamodel.routing_policy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.NetworkFactory.NetworkFactoryBuilder;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.statement.Statement;

@JsonSchemaDescription(
    "A procedural routing policy used to transform and accept/reject IPV4/IPV6 routes")
public class RoutingPolicy extends ComparableStructure<String> {

  public static class Builder extends NetworkFactoryBuilder<RoutingPolicy> {

    private String _name;

    private Configuration _owner;

    private List<Statement> _statements;

    public Builder(NetworkFactory networkFactory) {
      super(networkFactory, RoutingPolicy.class);
      _statements = Collections.emptyList();
    }

    @Override
    public RoutingPolicy build() {
      String name = _name != null ? _name : generateName();
      RoutingPolicy routingPolicy = new RoutingPolicy(name, _owner);
      if (_owner != null) {
        _owner.getRoutingPolicies().put(name, routingPolicy);
      }
      routingPolicy.setStatements(_statements);
      return routingPolicy;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }

    public Builder setOwner(Configuration owner) {
      _owner = owner;
      return this;
    }

    public Builder setStatements(List<Statement> statements) {
      _statements = statements;
      return this;
    }
  }

  public static boolean isGenerated(String s) {
    return s.startsWith("~");
  }

  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_STATEMENTS = "statements";

  private Configuration _owner;

  private transient Set<String> _sources;

  private List<Statement> _statements;

  @JsonCreator
  private RoutingPolicy(@JsonProperty(PROP_NAME) String name) {
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

  public Set<String> computeSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    if (_sources == null) {
      Set<String> newParentSources = Sets.union(parentSources, ImmutableSet.of(_key));
      ImmutableSet.Builder<String> childSources = ImmutableSet.builder();
      childSources.add(_key);
      for (Statement statement : _statements) {
        childSources.addAll(statement.collectSources(newParentSources, routingPolicies, w));
      }
      _sources = childSources.build();
    }
    return _sources;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof RoutingPolicy)) {
      return false;
    }
    RoutingPolicy other = (RoutingPolicy) o;
    return _statements.equals(other._statements);
  }

  @JsonIgnore
  public Configuration getOwner() {
    return _owner;
  }

  @JsonIgnore
  public Set<String> getSources() {
    return _sources;
  }

  @JsonProperty(PROP_STATEMENTS)
  @JsonPropertyDescription("The list of routing-policy statements to execute")
  public List<Statement> getStatements() {
    return _statements;
  }

  public boolean process(
      AbstractRoute inputRoute,
      AbstractRouteBuilder<?, ?> outputRoute,
      @Nullable Ip peerAddress,
      String vrf,
      Direction direction) {
    return process(inputRoute, outputRoute, peerAddress, null, vrf, direction);
  }

  public boolean process(
      AbstractRoute inputRoute,
      AbstractRouteBuilder<?, ?> outputRoute,
      @Nullable Ip peerAddress,
      @Nullable Prefix peerPrefix,
      String vrf,
      Direction direction) {
    Environment environment =
        Environment.builder(_owner)
            .setVrf(vrf)
            .setOriginalRoute(inputRoute)
            .setOutputRoute(outputRoute)
            .setPeerAddress(peerAddress)
            .setDirection(direction)
            .setPeerPrefix(peerPrefix)
            .build();
    Result result = call(environment);
    return result.getBooleanValue();
  }

  @JsonProperty(PROP_STATEMENTS)
  public void setStatements(List<Statement> statements) {
    _statements = statements;
  }

  public RoutingPolicy simplify() {
    ImmutableList.Builder<Statement> simpleStatements = ImmutableList.builder();
    for (Statement statement : _statements) {
      simpleStatements.addAll(statement.simplify());
    }
    RoutingPolicy simple = new RoutingPolicy(_key, _owner);
    simple.setStatements(simpleStatements.build());
    return simple;
  }
}
