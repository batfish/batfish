package org.batfish.datamodel.routing_policy;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.NetworkFactory.NetworkFactoryBuilder;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A procedural routing policy used to transform and accept/reject IPV4/IPV6 routes */
public class RoutingPolicy implements Serializable {

  /**
   * Builder for {@link RoutingPolicy}.
   *
   * <p><b>Note:</b> the resulting statements will be an immutable list.
   */
  public static final class Builder extends NetworkFactoryBuilder<RoutingPolicy> {

    private String _name;
    private Configuration _owner;
    private ImmutableList.Builder<Statement> _statements;

    public Builder(NetworkFactory networkFactory) {
      super(networkFactory, RoutingPolicy.class);
      _statements = ImmutableList.builder();
    }

    @Override
    public @Nonnull RoutingPolicy build() {
      String name = _name != null ? _name : generateName();
      RoutingPolicy routingPolicy = new RoutingPolicy(name, _owner);
      if (_owner != null) {
        _owner.getRoutingPolicies().put(name, routingPolicy);
      }
      routingPolicy.setStatements(_statements.build());
      return routingPolicy;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setOwner(Configuration owner) {
      _owner = owner;
      return this;
    }

    public @Nonnull Builder addStatement(@Nonnull Statement statement) {
      _statements.add(statement);
      return this;
    }

    public @Nonnull Builder setStatements(@Nonnull List<Statement> statements) {
      _statements = ImmutableList.<Statement>builder().addAll(statements);
      return this;
    }
  }

  private static final long serialVersionUID = 1L;
  private static final String PROP_NAME = "name";
  private static final String PROP_STATEMENTS = "statements";

  @Nonnull private final String _name;
  @Nullable private Configuration _owner;
  @Nullable private transient Set<String> _sources;
  @Nonnull private List<Statement> _statements;

  @JsonCreator
  private RoutingPolicy(@Nullable @JsonProperty(PROP_NAME) String name) {
    this(requireNonNull(name), null);
  }

  public RoutingPolicy(@Nonnull String name, @Nullable Configuration owner) {
    _name = name;
    _owner = owner;
    _statements = new ArrayList<>();
  }

  /** Builder to be used with a {@link NetworkFactory} for tests */
  public static Builder builder(@Nullable NetworkFactory nf) {
    return new Builder(nf);
  }

  public static Builder builder() {
    return new Builder(null);
  }

  public static boolean isGenerated(String s) {
    return s.startsWith("~");
  }

  public Result call(Environment environment) {
    for (Statement statement : _statements) {
      Result result = statement.execute(environment);
      if (result.getExit()) {
        return result;
      }
      if (result.getReturn()) {
        return result.toBuilder().setReturn(false).build();
      }
    }
    return Result.builder()
        .setFallThrough(true)
        .setBooleanValue(environment.getDefaultAction())
        .build();
  }

  public Set<String> computeSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    if (_sources == null) {
      Set<String> newParentSources = Sets.union(parentSources, ImmutableSet.of(_name));
      ImmutableSet.Builder<String> childSources = ImmutableSet.builder();
      childSources.add(_name);
      for (Statement statement : _statements) {
        childSources.addAll(statement.collectSources(newParentSources, routingPolicies, w));
      }
      _sources = childSources.build();
    }
    return _sources;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoutingPolicy)) {
      return false;
    }
    RoutingPolicy policy = (RoutingPolicy) o;
    // Skip owner, sources
    return Objects.equals(_name, policy._name) && Objects.equals(_statements, policy._statements);
  }

  @Override
  public int hashCode() {
    // Skip owner, sources
    return Objects.hash(_name, _statements);
  }

  /** Return the name of this policy */
  @JsonProperty(PROP_NAME)
  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  @JsonIgnore
  public Configuration getOwner() {
    return _owner;
  }

  @Nullable
  @JsonIgnore
  public Set<String> getSources() {
    return _sources;
  }

  /** Returns the list of routing-policy statements to execute */
  @Nonnull
  @JsonProperty(PROP_STATEMENTS)
  public List<Statement> getStatements() {
    return _statements;
  }

  public boolean process(
      AbstractRouteDecorator inputRoute,
      AbstractRouteBuilder<?, ?> outputRoute,
      Ip peerAddress,
      String vrf,
      Direction direction) {
    return process(inputRoute, outputRoute, peerAddress, null, vrf, direction);
  }

  /**
   * @param peerAddress The address of a known peer.
   * @param peerPrefix The address of an unknown peer. Used for dynamic BGP.
   * @return True if the policy accepts the route.
   */
  public boolean process(
      AbstractRouteDecorator inputRoute,
      AbstractRouteBuilder<?, ?> outputRoute,
      @Nullable Ip peerAddress,
      @Nullable Prefix peerPrefix,
      String vrf,
      Direction direction) {
    checkState(_owner != null, "Cannot evaluate routing policy without a Configuration");
    Environment environment =
        Environment.builder(_owner, vrf)
            .setOriginalRoute(inputRoute)
            .setOutputRoute(outputRoute)
            .setPeerAddress(peerAddress)
            .setDirection(direction)
            .setPeerPrefix(peerPrefix)
            .build();
    Result result = call(environment);
    return result.getBooleanValue() && !(Boolean.TRUE.equals(environment.getSuppressed()));
  }

  @JsonProperty(PROP_STATEMENTS)
  public void setStatements(@Nullable List<Statement> statements) {
    _statements = firstNonNull(statements, ImmutableList.of());
  }

  public RoutingPolicy simplify() {
    ImmutableList.Builder<Statement> simpleStatements = ImmutableList.builder();
    for (Statement statement : _statements) {
      simpleStatements.addAll(statement.simplify());
    }
    RoutingPolicy simple = new RoutingPolicy(_name, _owner);
    simple.setStatements(simpleStatements.build());
    return simple;
  }
}
