package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/**
 * Juniper subroutine chain. Evaluates a route against a series of routing policies in order.
 * Returns a {@link Result} with a boolean value of true if all of the top-level policies accept the
 * route.
 *
 * <p>See more info on chains:
 * https://www.juniper.net/documentation/en_US/junos/topics/concept/policy-routing-policies-chain-evaluation-method.html
 */
@ParametersAreNonnullByDefault
public final class ConjunctionChain extends BooleanExpr {

  private static final long serialVersionUID = 1L;

  private static final String PROP_SUBROUTINES = "subroutines";

  @Nonnull private final List<BooleanExpr> _subroutines;

  @JsonCreator
  private static ConjunctionChain create(
      @Nullable @JsonProperty(PROP_SUBROUTINES) List<BooleanExpr> subroutines) {
    return new ConjunctionChain(firstNonNull(subroutines, ImmutableList.of()));
  }

  public ConjunctionChain(List<BooleanExpr> subroutines) {
    _subroutines = ImmutableList.copyOf(subroutines);
  }

  @Override
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    ImmutableSet.Builder<String> childSources = ImmutableSet.builder();
    for (BooleanExpr conjunct : _subroutines) {
      childSources.addAll(conjunct.collectSources(parentSources, routingPolicies, w));
    }
    return childSources.build();
  }

  @Override
  public Result evaluate(Environment environment) {
    /* TODO
    It is unclear what result's value for _return means here, and it doesn't currently have any
    impact in any context where ConjunctionChain is used. For now, just set it to false.
     */
    Result subroutineResult = new Result();
    // By default move on to the next policy
    subroutineResult.setFallThrough(true);
    for (BooleanExpr subroutine : _subroutines) {
      subroutineResult = subroutine.evaluate(environment);
      if (subroutineResult.getExit()) {
        // Reached an exit/terminal action. Return regardless of boolean value
        return subroutineResult;
      } else if (!subroutineResult.getFallThrough() && !subroutineResult.getBooleanValue()) {
        // Found first match that returns false, short-circuit here
        subroutineResult.setReturn(false);
        return subroutineResult;
      }
    }
    // Check if we are allowed to fall through to the default policy, if not, return last result
    if (!subroutineResult.getFallThrough()) {
      subroutineResult.setReturn(false);
      return subroutineResult;
    } else {
      String defaultPolicy = environment.getDefaultPolicy();
      if (defaultPolicy != null) {
        CallExpr callDefaultPolicy = new CallExpr(environment.getDefaultPolicy());
        Result defaultPolicyResult = callDefaultPolicy.evaluate(environment);
        defaultPolicyResult.setReturn(false);
        return defaultPolicyResult;
      } else {
        throw new BatfishException("Default policy is not set");
      }
    }
  }

  @Nonnull
  @JsonProperty(PROP_SUBROUTINES)
  public List<BooleanExpr> getSubroutines() {
    return _subroutines;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConjunctionChain)) {
      return false;
    }
    ConjunctionChain that = (ConjunctionChain) o;
    return Objects.equals(_subroutines, that._subroutines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_subroutines);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("subroutines", _subroutines).toString();
  }
}
