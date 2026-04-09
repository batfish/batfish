package org.batfish.datamodel.vendor_family.cisco;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class LoggingMatchers {

  /** Provides a matcher that matches if logging is on. */
  public static Matcher<Logging> isOn() {
    return new HasOn(equalTo(true));
  }

  private LoggingMatchers() {}

  private static final class HasOn extends FeatureMatcher<Logging, Boolean> {
    HasOn(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "is logging with on", "on");
    }

    @Override
    protected Boolean featureValueOf(Logging actual) {
      return actual.getOn();
    }
  }
}
