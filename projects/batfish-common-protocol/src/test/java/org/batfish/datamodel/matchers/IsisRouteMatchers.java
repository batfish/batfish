package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.matchers.IsisRouteMatchersImpl.HasDown;
import org.batfish.datamodel.matchers.IsisRouteMatchersImpl.IsIsisRouteThat;
import org.hamcrest.Matcher;

public final class IsisRouteMatchers {

  public static @Nonnull Matcher<IsisRoute> hasDown() {
    return new HasDown(equalTo(true));
  }

  public static @Nonnull Matcher<AbstractRoute> isIsisRouteThat(
      @Nonnull Matcher<? super IsisRoute> subMatcher) {
    return new IsIsisRouteThat(subMatcher);
  }

  private IsisRouteMatchers() {}
}
