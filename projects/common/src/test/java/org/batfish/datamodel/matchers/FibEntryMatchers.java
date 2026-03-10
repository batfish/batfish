package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FibAction;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.matchers.FibEntryMatchersImpl.HasAction;
import org.hamcrest.Matcher;

/** Matchers for {@link FibEntry} */
@ParametersAreNonnullByDefault
public final class FibEntryMatchers {
  public static @Nonnull Matcher<FibEntry> hasAction(Matcher<? super FibAction> subMatcher) {
    return new HasAction(subMatcher);
  }

  private FibEntryMatchers() {}
}
