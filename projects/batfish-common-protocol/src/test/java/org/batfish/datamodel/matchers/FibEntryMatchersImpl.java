package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FibAction;
import org.batfish.datamodel.FibEntry;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class FibEntryMatchersImpl {

  static final class HasAction extends FeatureMatcher<FibEntry, FibAction> {

    public HasAction(Matcher<? super FibAction> subMatcher) {
      super(subMatcher, "A FibEntry with action", "action");
    }

    @Override
    protected @Nonnull FibAction featureValueOf(FibEntry fibEntry) {
      return fibEntry.getAction();
    }
  }

  private FibEntryMatchersImpl() {}
}
