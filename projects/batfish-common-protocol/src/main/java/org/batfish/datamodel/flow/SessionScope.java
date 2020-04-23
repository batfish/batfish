package org.batfish.datamodel.flow;

public interface SessionScope {
  <T> T accept(SessionScopeVisitor<T> visitor);
}
