package org.batfish.symbolic.ainterpreter;

import java.util.Objects;
import net.sf.javabdd.BDD;

public class ReachabilityDomainElement {

  private BDD _underApproximation;

  private BDD _overApproximation;

  private BDD _blockedAcls;

  public ReachabilityDomainElement(BDD under, BDD over, BDD acls) {
    this._underApproximation = under;
    this._overApproximation = over;
    this._blockedAcls = acls;
  }

  public BDD getUnderApproximation() {
    return _underApproximation;
  }

  public BDD getOverApproximation() {
    return _overApproximation;
  }

  public BDD getBlockedAcls() {
    return _blockedAcls;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReachabilityDomainElement that = (ReachabilityDomainElement) o;
    return Objects.equals(_underApproximation, that._underApproximation)
        && Objects.equals(_overApproximation, that._overApproximation)
        && Objects.equals(_blockedAcls, that._blockedAcls);
  }

  @Override
  public int hashCode() {

    return Objects.hash(_underApproximation, _overApproximation, _blockedAcls);
  }
}
