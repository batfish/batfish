package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class CommunityListMatchers {

  private static final class HasLine extends FeatureMatcher<CommunityList, CommunityListLine> {

    private final int _index;

    private HasLine(int index, @Nonnull Matcher<? super CommunityListLine> subMatcher) {
      super(
          subMatcher,
          String.format("A CommunityList with line %d:", index),
          String.format("line %d", index));
      _index = index;
    }

    @Override
    protected CommunityListLine featureValueOf(CommunityList actual) {
      return actual.getLines().get(_index);
    }
  }

  public static @Nonnull Matcher<CommunityList> hasLine(
      int index, @Nonnull Matcher<? super CommunityListLine> subMatcher) {
    return new HasLine(index, subMatcher);
  }

  private CommunityListMatchers() {}
}
