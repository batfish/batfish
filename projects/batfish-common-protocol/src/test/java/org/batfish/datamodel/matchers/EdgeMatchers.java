package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.matchers.EdgeMatchersImpl.HasInt1;
import org.batfish.datamodel.matchers.EdgeMatchersImpl.HasInt2;
import org.batfish.datamodel.matchers.EdgeMatchersImpl.HasNode1;
import org.batfish.datamodel.matchers.EdgeMatchersImpl.HasNode2;
import org.hamcrest.Matcher;

public final class EdgeMatchers {

  /**
   * @param subMatcher a {@Matcher} for the sending interface of the {@Edge}.
   * @return A {@Matcher} for the {@Edge}.
   */
  public static HasInt1 hasInt1(@Nonnull Matcher<? super String> subMatcher) {
    return new HasInt1(subMatcher);
  }

  /**
   * @param subMatcher a {@Matcher} for the receiving interface of the {@Edge}.
   * @return A {@Matcher} for the {@Edge}.
   */
  public static HasInt2 hasInt2(@Nonnull Matcher<? super String> subMatcher) {
    return new HasInt2(subMatcher);
  }

  /**
   * @param int2 the expected receiving interface of the {@Edge}.
   * @return A {@Matcher} for the {@Edge}.
   */
  public static HasInt2 hasInt2(@Nonnull String int2) {
    return new HasInt2(equalTo(int2));
  }

  /**
   * @param subMatcher a {@Matcher} for the sending node of the {@Edge}.
   * @return A {@Matcher} for the {@Edge}.
   */
  public static HasNode1 hasNode1(@Nonnull Matcher<? super String> subMatcher) {
    return new HasNode1(subMatcher);
  }

  /**
   * @param subMatcher a {@Matcher} for the receiving node of the {@Edge}.
   * @return A {@Matcher} for the {@Edge}.
   */
  public static HasNode2 hasNode2(@Nonnull Matcher<? super String> subMatcher) {
    return new HasNode2(subMatcher);
  }

  private EdgeMatchers() {}
}
