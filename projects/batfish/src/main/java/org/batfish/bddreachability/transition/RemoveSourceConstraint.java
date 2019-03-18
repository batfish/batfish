package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDSourceManager;

/**
 * Model the packet exiting a node. The {@link BDDSourceManager} enforces that the source interface
 * variable is constrained to be a valid value while within the node, and erases the constrain when
 * exiting.
 */
final class RemoveSourceConstraint implements Transition {
  private final BDDSourceManager _mgr;

  RemoveSourceConstraint(BDDSourceManager mgr) {
    checkArgument(
        !mgr.isTrivial(),
        "RemoveSourceConstraint for a Trivial BDDSourceManager. Use Identity instead");
    _mgr = mgr;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RemoveSourceConstraint)) {
      return false;
    }
    RemoveSourceConstraint that = (RemoveSourceConstraint) o;
    return Objects.equals(_mgr, that._mgr);
  }

  @Override
  public int hashCode() {

    return Objects.hash(_mgr);
  }

  @Override
  public BDD transitForward(BDD bdd) {
    assert _mgr.hasIsValidConstraint(bdd);
    return _mgr.existsSource(bdd);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    assert !_mgr.hasSourceConstraint(bdd);
    return bdd.and(_mgr.isValidValue());
  }
}
