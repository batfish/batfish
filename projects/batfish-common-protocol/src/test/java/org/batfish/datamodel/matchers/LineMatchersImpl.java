package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.vendor_family.cisco.Line;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class LineMatchersImpl {

  static final class RequiresAuthentication extends FeatureMatcher<Line, Boolean> {
    RequiresAuthentication(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "a Line that requires authentication", "requires authentication");
    }

    @Override
    protected Boolean featureValueOf(Line actual) {
      return actual.requiresAuthentication();
    }
  }
}
