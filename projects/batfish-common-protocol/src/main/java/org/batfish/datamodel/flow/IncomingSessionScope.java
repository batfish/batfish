package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a session that can match flows entering a given set of interfaces */
@ParametersAreNonnullByDefault
public final class IncomingSessionScope implements SessionScope {
  private static final String PROP_INCOMING_INTERFACES = "incomingInterfaces";

  private final @Nonnull Set<String> _incomingInterfaces;

  public IncomingSessionScope(Set<String> incomingInterfaces) {
    _incomingInterfaces = ImmutableSet.copyOf(incomingInterfaces);
  }

  @JsonCreator
  private static IncomingSessionScope jsonCreator(
      @JsonProperty(PROP_INCOMING_INTERFACES) @Nullable Set<String> incomingInterfaces) {
    checkNotNull(incomingInterfaces, "Missing %s", PROP_INCOMING_INTERFACES);
    return new IncomingSessionScope(incomingInterfaces);
  }

  @Override
  public <T> T accept(SessionScopeVisitor<T> visitor) {
    return visitor.visitIncomingSessionScope(this);
  }

  @JsonProperty(PROP_INCOMING_INTERFACES)
  public @Nonnull Set<String> getIncomingInterfaces() {
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
