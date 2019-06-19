package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.StubSettings;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class StubSettingsMatchersImpl {

  static final class HasSuppressType3 extends FeatureMatcher<StubSettings, Boolean> {
    HasSuppressType3(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A StubSettings with suppressType3:", "suppressType3");
    }

    @Override
    protected Boolean featureValueOf(StubSettings actual) {
      return actual.getSuppressType3();
    }
  }

  private StubSettingsMatchersImpl() {}
}
