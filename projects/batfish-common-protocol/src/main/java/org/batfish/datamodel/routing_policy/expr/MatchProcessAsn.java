package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.EigrpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that evaluates whether an {@link Environment} has an EIGRP route with an ASN
 * equal to some given ASN.
 */
public final class MatchProcessAsn extends BooleanExpr {

  private static final String PROP_PROCESS_ASN = "asn";

  @Nonnull private final Set<Long> _asn;

  public MatchProcessAsn(long asn) {
    this(ImmutableSet.of(asn));
  }

  public MatchProcessAsn(Collection<Long> asn) {
    checkArgument(!asn.isEmpty(), "Must match at least 1 asn");
    _asn = asn.stream().sorted().collect(ImmutableSet.toImmutableSet());
  }

  @JsonCreator
  private static MatchProcessAsn create(@Nullable @JsonProperty(PROP_PROCESS_ASN) Set<Long> asn) {
    checkArgument(!asn.isEmpty(), "Missing %s", PROP_PROCESS_ASN);
    return new MatchProcessAsn(asn);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchProcessAsn(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    if (!(environment.getOriginalRoute() instanceof EigrpRoute)) {
      return new Result(false);
    }
    EigrpRoute route = (EigrpRoute) environment.getOriginalRoute();
    return new Result(_asn.stream().anyMatch(s -> s == route.getProcessAsn()));
  }

  @Nonnull
  @JsonProperty(PROP_PROCESS_ASN)
  public Set<Long> getAsn() {
    return _asn;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchProcessAsn)) {
      return false;
    }
    return _asn.equals(((MatchProcessAsn) obj)._asn);
  }

  @Override
  public int hashCode() {
    return _asn.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + Arrays.toString(_asn.toArray()) + ">";
  }
}
