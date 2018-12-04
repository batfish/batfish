package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

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
}
