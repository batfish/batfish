package org.batfish.datamodel.routing_policy;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.expr.RibExpr;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.trace.Tracer;

/** A procedural routing policy used to transform and accept/reject IPV4/IPV6 routes */
public class RoutingPolicy implements Serializable {

  /**
   * Builder for {@link RoutingPolicy}.
   *
   * <p><b>Note:</b> the resulting statements will be an immutable list.
   */
  public static final class Builder {

    private @Nullable String _name;
    private @Nullable Supplier<String> _nameGenerator;
    private Configuration _owner;
    private ImmutableList.Builder<Statement> _statements;

    private Builder(@Nullable Supplier<String> nameGenerator) {
      _nameGenerator = nameGenerator;
      _statements = ImmutableList.builder();
    }

    public @Nonnull RoutingPolicy build() {
      checkArgument(_name != null || _nameGenerator != null, "Must set name before building");
      String name = _name != null ? _name : _nameGenerator.get();
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

  public static Builder builder() {
    return new Builder(null);
  }

  public static Builder builder(Supplier<String> nameGenerator) {
    return new Builder(nameGenerator);
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
      Set<String> newParentSources =
          ImmutableSet.<String>builderWithExpectedSize(parentSources.size() + 1)
              .addAll(parentSources)
              .add(_name)
              .build();
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

  /**
   * @return True if the policy accepts the route. Clients should only call this when state and
   *     transformations are not needed.
   */
  public boolean processReadOnly(AbstractRouteDecorator inputRoute) {
    // arbitrarily choose OUT direction, BGP route builder.
    return process(inputRoute, Bgpv4Route.builder(), (EigrpProcess) null, Direction.OUT, null);
  }

  /** @return True if the policy accepts the route. */
  public boolean process(
      AbstractRouteDecorator inputRoute,
      AbstractRouteBuilder<?, ?> outputRoute,
      Direction direction) {
    return process(inputRoute, outputRoute, null, null, direction, null, null);
  }

  /** @return True if the policy accepts the route. */
  public boolean process(
      AbstractRouteDecorator inputRoute,
      AbstractRouteBuilder<?, ?> outputRoute,
      Direction direction,
      Tracer tracer) {
    return process(inputRoute, outputRoute, null, null, direction, null, tracer);
  }

  /** @return True if the policy accepts the route. */
  public boolean process(
      AbstractRouteDecorator inputRoute,
      AbstractRouteBuilder<?, ?> outputRoute,
      Direction direction,
      BiFunction<RibExpr, PrefixSpace, Boolean> ribIntersectsPrefixSpaceEvaluator) {
    return process(
        inputRoute, outputRoute, null, null, direction, ribIntersectsPrefixSpaceEvaluator, null);
  }

  public boolean process(
      @Nonnull AbstractRouteDecorator inputRoute,
      @Nonnull AbstractRouteBuilder<?, ?> outputRoute,
      @Nonnull EigrpProcess eigrpProcess,
      Direction direction) {
    return process(inputRoute, outputRoute, null, eigrpProcess, direction, null, null);
  }

  public boolean process(
      @Nonnull AbstractRouteDecorator inputRoute,
      @Nonnull AbstractRouteBuilder<?, ?> outputRoute,
      @Nonnull EigrpProcess eigrpProcess,
      Direction direction,
      BiFunction<RibExpr, PrefixSpace, Boolean> ribIntersectsPrefixSpaceEvaluator) {
    return process(
        inputRoute,
        outputRoute,
        null,
        eigrpProcess,
        direction,
        ribIntersectsPrefixSpaceEvaluator,
        null);
  }

  /**
   * Process a given {@code inputRoute} through this BGP routing policy.
   *
   * @param inputRoute Input route to process
   * @param outputRoute Builder for output BGP route; may be modified by policy
   * @param sessionProperties {@link BgpSessionProperties} representing the session <em>from</em>
   *     the remote node <em>to</em> the node processing the policy (regardless of whether this
   *     policy is currently being used for import or export)
   * @param direction {@link Direction} in which route is being sent
   * @param ribIntersectsPrefixSpaceEvaluator function that evaluates whether there is any
   *     intersection between the prefixes of the routes in the RIB represented by a provided {@link
   *     RibExpr} and a provided {@link PrefixSpace}
   */
  public boolean processBgpRoute(
      AbstractRouteDecorator inputRoute,
      BgpRoute.Builder<?, ?> outputRoute,
      @Nullable BgpSessionProperties sessionProperties,
      Direction direction,
      @Nullable BiFunction<RibExpr, PrefixSpace, Boolean> ribIntersectsPrefixSpaceEvaluator) {
    checkState(_owner != null, "Cannot evaluate routing policy without a Configuration");
    return process(
        inputRoute,
        outputRoute,
        sessionProperties,
        null,
        direction,
        ribIntersectsPrefixSpaceEvaluator,
        null);
  }

  private boolean process(
      AbstractRouteDecorator inputRoute,
      AbstractRouteBuilder<?, ?> outputRoute,
      @Nullable BgpSessionProperties bgpSessionProperties,
      @Nullable EigrpProcess eigrpProcess,
      Direction direction,
      @Nullable BiFunction<RibExpr, PrefixSpace, Boolean> ribIntersectsPrefixSpaceEvaluator,
      @Nullable Tracer tracer) {
    checkState(_owner != null, "Cannot evaluate routing policy without a Configuration");
    Environment environment =
        Environment.builder(_owner)
            .setBgpSessionProperties(bgpSessionProperties)
            .setOriginalRoute(inputRoute)
            .setOutputRoute(outputRoute)
            .setDirection(direction)
            .setEigrpProcess(eigrpProcess)
            .setRibIntersectsPrefixSpaceEvaluator(ribIntersectsPrefixSpaceEvaluator)
            .setTracer(tracer)
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
