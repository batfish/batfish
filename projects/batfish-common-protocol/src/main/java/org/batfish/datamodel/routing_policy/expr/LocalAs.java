package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.routing_policy.Environment;

/** The local AS number of a BGP session. */
public final class LocalAs extends AsExpr {

  public static @Nonnull LocalAs instance() {
    return INSTANCE;
  }

  // private implementation details

  private static final LocalAs INSTANCE = new LocalAs();

  private LocalAs() {} // suppress instantiation

  @Override
  public long evaluate(Environment environment) {
    BgpSessionProperties sessionProps = environment.getBgpSessionProperties();
    checkState(sessionProps != null, "Expected BGP session properties");
    // NB: see javadoc for linked function
    return sessionProps.getHeadAs();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof LocalAs;
  }

  @Override
  public int hashCode() {
    return LocalAs.class.getCanonicalName().hashCode();
  }

  @Override
  public String toString() {
    return LocalAs.class.getSimpleName();
  }
}
