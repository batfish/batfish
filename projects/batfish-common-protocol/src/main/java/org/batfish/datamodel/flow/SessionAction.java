package org.batfish.datamodel.flow;

import org.batfish.datamodel.visitors.SessionActionVisitor;

/** An action that a firewall session takes for return traffic matching the session */
public interface SessionAction {
  <T> T accept(SessionActionVisitor<T> visitor);
}
