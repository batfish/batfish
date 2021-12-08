package org.batfish.datamodel.visitors;

import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.FibVtep;

public interface FibActionVisitor<T> {

  T visitFibForward(FibForward fibForward);

  T visitFibNextVrf(FibNextVrf fibNextVrf);

  T visitFibNullRoute(FibNullRoute fibNullRoute);

  T visitFibVtep(FibVtep fibVtep);
}
