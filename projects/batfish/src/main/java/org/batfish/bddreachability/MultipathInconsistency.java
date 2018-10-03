package org.batfish.bddreachability;

import net.sf.javabdd.BDD;
import org.batfish.z3.IngressLocation;

/*
 * Internal class representing a multipath inconsistency.
 */
class MultipathInconsistency {
  private final BDD _bdd;
  private final IngressLocation _ingressLocation;

  MultipathInconsistency(IngressLocation ingressLocation, BDD bdd) {
    this._ingressLocation = ingressLocation;
    this._bdd = bdd;
  }

  public BDD getBDD() {
    return _bdd;
  }

  public IngressLocation getIngressLocation() {
    return _ingressLocation;
  }
}
