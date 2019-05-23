package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDSourceManager;

/**
 * Model the packet entering a node. The {@link BDDSourceManager} sets the source interface variable
 * to the value for that interface. When traversing backwards, we erase the constraint.
 */
public final class AddSourceConstraint implements Transition {
  private final BDDSourceManager _mgr;
  private final BDD _sourceBdd;

  AddSourceConstraint(BDDSourceManager mgr, String iface) {
    checkArgument(
        !mgr.isTrivial(),
        "AddSourceConstraint for a Trivial BDDSourceManager. Use Identity instead");
    _mgr = mgr;
    _sourceBdd = mgr.getSourceInterfaceBDD(iface);
  }

  AddSourceConstraint(BDDSourceManager mgr) {
    checkArgument(
        !mgr.isTrivial(),
        "AddSourceConstraint for a Trivial BDDSourceManager. Use Identity instead");
    _mgr = mgr;
    _sourceBdd = mgr.getOriginatingFromDeviceBDD();
  }

  BDDSourceManager getSourceManager() {
    return _mgr;
  }

  BDD getSourceBdd() {
    return _sourceBdd;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    assert !_mgr.hasSourceConstraint(bdd) : "source constraint should not be present";
    return bdd.and(_sourceBdd);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    assert _mgr.hasIsValidConstraint(bdd) : "source constraint should be present";
    return _mgr.existsSource(bdd.and(_sourceBdd));
  }
}
