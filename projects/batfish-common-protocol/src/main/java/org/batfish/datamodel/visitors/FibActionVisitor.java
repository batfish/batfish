package org.batfish.datamodel.visitors;

import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNullRoute;

public interface FibActionVisitor<T> {

  T visitFibForward(FibForward fibForward);

  T visitFibNullRoute(FibNullRoute fibNullRoute);
}
