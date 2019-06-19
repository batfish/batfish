package org.batfish.datamodel.visitors;

import org.batfish.datamodel.flow.AcceptVrf;
import org.batfish.datamodel.flow.FibLookup;
import org.batfish.datamodel.flow.ForwardOutInterface;

/** A generic {@link org.batfish.datamodel.flow.SessionAction} visitor. */
public interface SessionActionVisitor<T> {

  T visitAcceptVrf(AcceptVrf acceptVrf);

  T visitFibLookup(FibLookup fibLookup);

  T visitForwardOutInterface(ForwardOutInterface forwardOutInterface);
}
