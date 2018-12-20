package org.batfish.representation.juniper;

import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.representation.juniper.NatRuleMatchToHeaderSpace.toHeaderSpace;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;

/** Represents a nat rule for Juniper */
@ParametersAreNonnullByDefault
public final class NatRule implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<NatRuleMatch> _matches;

  private final String _name;

  @Nullable private NatRuleThen _then;

  public NatRule(String name) {
    _matches = new LinkedList<>();
    _name = name;
    _then = null;
  }

  @Nonnull
  public List<NatRuleMatch> getMatches() {
    return _matches;
  }

  @Nullable
  public String getName() {
    return _name;
  }

  @Nullable
  public NatRuleThen getThen() {
    return _then;
  }

  public void setThen(@Nullable NatRuleThen then) {
    _then = then;
  }

  /** Convert to vendor-independent {@link Transformation}. */
  public Optional<Transformation.Builder> toTransformationBuilder(
      IpField field, Map<String, NatPool> pools) {
    Transformation.Builder builder = when(new MatchHeaderSpace(toHeaderSpace(_matches)));

    if (_then instanceof NatRuleThenPool) {
      NatPool pool = pools.get(((NatRuleThenPool) _then).getPoolName());
      if (pool == null) {
        // pool is undefined.
        return Optional.empty();
      }
      builder.apply(new AssignIpAddressFromPool(field, pool.getFromAddress(), pool.getToAddress()));
    } else if (_then instanceof NatRuleThenOff) {
      // don't transform
      builder.apply();
    } else {
      throw new IllegalArgumentException("Unrecognized NatRuleThen type");
    }

    return Optional.of(builder);
  }
}
