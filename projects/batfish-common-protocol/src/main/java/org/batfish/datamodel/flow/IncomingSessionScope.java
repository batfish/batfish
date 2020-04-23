package org.batfish.datamodel.flow;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class IncomingSessionScope implements SessionScope {
  private final @Nonnull Set<String> _incomingInterfaces;

  public IncomingSessionScope(Set<String> incomingInterfaces) {
    _incomingInterfaces = ImmutableSet.copyOf(incomingInterfaces);
  }

  @Override
  public <T> T accept(SessionScopeVisitor<T> visitor) {
    return visitor.visitIncomingSessionScope(this);
  }

  @Nonnull
  public Set<String> getIncomingInterfaces() {
    return _incomingInterfaces;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof IncomingSessionScope)) {
      return false;
    }
    return _incomingInterfaces.equals(((IncomingSessionScope) obj)._incomingInterfaces);
  }

  @Override
  public int hashCode() {
    return _incomingInterfaces.hashCode();
  }
}
