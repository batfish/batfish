package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.TransformationStep;

/** A {@link NatRule} that NATs using the configured IP Prefix. */
@ParametersAreNonnullByDefault
public class NatRuleThenPrefix implements NatRuleThen, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  private NatRuleThenPrefix(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleThenPrefixName)) {
      return false;
    }
    NatRuleThenPrefix that = (NatRuleThenPrefix) o;
    return Objects.equals(_prefix, that._prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix);
  }

  @Override
  public List<TransformationStep> toTransformationSteps(Nat nat, Ip interfaceIp) {
    throw new BatfishException("TODO");
  }
}
