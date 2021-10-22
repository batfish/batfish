package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.Environment;

/** The remote AS number of a BGP session. */
public final class RemoteAs extends AsExpr {

  public static @Nonnull RemoteAs instance() {
    return INSTANCE;
  }

  // private implementation details

  private static final RemoteAs INSTANCE = new RemoteAs();

  private RemoteAs() {} // suppress instantiation

  @Override
  public long evaluate(Environment environment) {
    Optional<Long> remoteAs = environment.getRemoteAs();
    checkState(remoteAs.isPresent(), "Expected BGP session properties");
    return remoteAs.get();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof RemoteAs;
  }

  @Override
  public int hashCode() {
    return RemoteAs.class.getCanonicalName().hashCode();
  }

  @Override
  public String toString() {
    return RemoteAs.class.getSimpleName();
  }
}
