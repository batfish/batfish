package org.batfish.datamodel.visitors;

import org.batfish.datamodel.flow.Accept;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.datamodel.flow.PostNatFibLookup;
import org.batfish.datamodel.flow.PreNatFibLookup;

/** A generic {@link org.batfish.datamodel.flow.SessionAction} visitor. */
public interface SessionActionVisitor<T> {

  T visitAcceptVrf(Accept acceptVrf);

  T visitPostNatFibLookup(PostNatFibLookup postNatFibLookup);

  T visitPreNatFibLookup(PreNatFibLookup preNatFibLookup);

  T visitForwardOutInterface(ForwardOutInterface forwardOutInterface);
}
