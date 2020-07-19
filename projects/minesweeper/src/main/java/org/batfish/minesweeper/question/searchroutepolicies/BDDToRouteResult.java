package org.batfish.minesweeper.question.searchroutepolicies;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Bgpv4Route;

/* The result of trying to convert a BDD to a route --
  we either produce a route satisfying the BDD or identify
  a set of unsatisfiable constraints.
*/
@ParametersAreNonnullByDefault
class BDDToRouteResult {
  // exactly one of these will be nonnull
  @Nullable private Bgpv4Route _route;
  @Nullable private BDD _unsatConstraints;

  BDDToRouteResult(Bgpv4Route route) {
    _route = route;
  }

  BDDToRouteResult(BDD unsatConstraints) {
    _unsatConstraints = unsatConstraints;
  }

  @Nullable
  Bgpv4Route getRoute() {
    return _route;
  }

  @Nullable
  BDD getUnsatConstraints() {
    return _unsatConstraints;
  }
}
