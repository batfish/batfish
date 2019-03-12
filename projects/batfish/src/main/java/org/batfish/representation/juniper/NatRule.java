package org.batfish.representation.juniper;

import static org.batfish.datamodel.transformation.Transformation.when;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

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
      JuniperConfiguration config, Nat nat, Ip interfaceIp) {

    List<TransformationStep> steps =
        _then == null ? null : _then.toTransformationSteps(config, nat, interfaceIp, false);

    MatchHeaderSpace match =
        new MatchHeaderSpace(NatRuleMatchToHeaderSpace.toHeaderSpace(_matches));

    // steps can be empty when the pool used by the rule is not found
    return (_then == null || steps.isEmpty())
        ? Optional.empty()
        : Optional.of(when(match).apply(steps));
  }

  /** Convert to vendor-independent {@link Transformation}. */
  public Optional<Transformation.Builder> toTransformationBuilderReverse(
      JuniperConfiguration config, Nat nat, Ip interfaceIp) {

    List<TransformationStep> steps =
        _then == null ? null : _then.toTransformationSteps(config, nat, interfaceIp, true);

    MatchHeaderSpace match =
        new MatchHeaderSpace(ReverseNatRuleMatchToHeaderSpace.toHeaderSpace(_matches));

    // steps can be empty when the pool used by the rule is not found
    return (_then == null || steps.isEmpty())
        ? Optional.of(when(match).apply(ImmutableList.of()))
        : Optional.of(when(match).apply(steps));
  }
}
