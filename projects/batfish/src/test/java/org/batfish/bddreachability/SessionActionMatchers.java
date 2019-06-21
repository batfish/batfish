package org.batfish.bddreachability;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.datamodel.flow.SessionAction;
import org.batfish.datamodel.matchers.IsInstanceThat;
import org.hamcrest.Matcher;

/** Matchers for {@link SessionAction}. */
@ParametersAreNonnullByDefault
public final class SessionActionMatchers {

  public static @Nonnull Matcher<SessionAction> isForwardOutInterfaceThat(
      Matcher<? super ForwardOutInterface> subMatcher) {
    return new IsInstanceThat<>(ForwardOutInterface.class, subMatcher);
  }
}
