package org.batfish.datamodel.flow;

public interface SessionScopeVisitor<T> {

  T visitIncomingSessionScope(IncomingSessionScope incomingSessionScope);

  T visitOriginatingSessionScope(OriginatingSessionScope originatingSessionScope);
}
