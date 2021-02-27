package org.batfish.common.matchers;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class WarningsMatchersImpl {

  static final class HasParseWarnings extends FeatureMatcher<Warnings, List<ParseWarning>> {
    HasParseWarnings(@Nonnull Matcher<? super List<ParseWarning>> subMatcher) {
      super(subMatcher, "Warnings with parse warnings:", "parse warnings");
    }

    @Override
    protected List<ParseWarning> featureValueOf(Warnings actual) {
      return actual.getParseWarnings();
    }
  }

  static final class HasRedFlags extends FeatureMatcher<Warnings, Set<Warning>> {
    HasRedFlags(@Nonnull Matcher<? super Set<Warning>> subMatcher) {
      super(subMatcher, "Warnings with redFlag warnings:", "redFlag warnings");
    }

    @Override
    protected Set<Warning> featureValueOf(Warnings actual) {
      return actual.getRedFlagWarnings();
    }
  }

  static final class HasPedanticWarnings extends FeatureMatcher<Warnings, Set<Warning>> {
    HasPedanticWarnings(@Nonnull Matcher<? super Set<Warning>> subMatcher) {
      super(subMatcher, "Warnings with pedantic warnings:", "pedantic warnings");
    }

    @Override
    protected Set<Warning> featureValueOf(Warnings actual) {
      return actual.getPedanticWarnings();
    }
  }

  static final class HasUnimplementedWarnings extends FeatureMatcher<Warnings, Set<Warning>> {
    HasUnimplementedWarnings(@Nonnull Matcher<? super Set<Warning>> subMatcher) {
      super(subMatcher, "Warnings with unimplemented warnings:", "unimplemented warnings");
    }

    @Override
    protected Set<Warning> featureValueOf(Warnings actual) {
      return actual.getUnimplementedWarnings();
    }
  }

  private WarningsMatchersImpl() {}
}
