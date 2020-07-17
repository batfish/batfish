package org.batfish.minesweeper.question.searchroutepolicies;

import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Bgpv4Route;

/* The result of trying to convert a BDD to a route --
  either we are able to produce a route satisfying the BDD or identify
  a set of unsatisfiable constraints.
*/
class BDDToRouteResult {
  private Bgpv4Route _route;
  private BDD _unsatConstraints;

  BDDToRouteResult(@Nonnull Bgpv4Route route) {
    _route = route;
  }

  BDDToRouteResult(@Nonnull BDD unsatConstraints) {
    _unsatConstraints = unsatConstraints;
  }

  Bgpv4Route getRoute() {
    return _route;
  }

  BDD getUnsatConstraints() {
    return _unsatConstraints;
  }
}
