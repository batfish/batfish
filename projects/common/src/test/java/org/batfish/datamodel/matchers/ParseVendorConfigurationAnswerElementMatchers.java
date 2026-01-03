package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class ParseVendorConfigurationAnswerElementMatchers {

  static final class HasParseWarning
      extends TypeSafeDiagnosingMatcher<ParseVendorConfigurationAnswerElement> {

    private final @Nonnull Matcher<? super String> _subMatcher;

    private final @Nonnull String _filename;

    HasParseWarning(@Nonnull String filename, @Nonnull Matcher<? super String> subMatcher) {
      _filename = filename;
      _subMatcher = subMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description
          .appendText("A ParseVendorConfigurationAnswerElement with a parse warning with text:")
          .appendDescriptionOf(_subMatcher);
    }

    @Override
    protected boolean matchesSafely(
        ParseVendorConfigurationAnswerElement item, Description mismatchDescription) {
      Warnings warnings = item.getWarnings().get(_filename);
      if (warnings == null) {
        mismatchDescription.appendText(String.format("No warnings for filename '%s'", _filename));
        return false;
      }
      if (warnings.getParseWarnings().stream()
          .map(ParseWarning::getComment)
          .noneMatch(_subMatcher::matches)) {
        mismatchDescription.appendText(
            String.format("No parse warnings for filename '%s' match", _filename));
        return false;
      }
      return true;
    }
  }

  private ParseVendorConfigurationAnswerElementMatchers() {}
}
