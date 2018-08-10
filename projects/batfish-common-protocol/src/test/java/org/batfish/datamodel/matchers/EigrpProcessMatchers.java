package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.batfish.datamodel.matchers.EigrpProcessMatchersImpl.HasAsn;
import org.batfish.datamodel.matchers.EigrpProcessMatchersImpl.HasMode;
import org.hamcrest.Matcher;

public final class EigrpProcessMatchers {

  private EigrpProcessMatchers() {}

  /** Provides a matcher that matches if the {@link EigrpProcess}'s asn is {@code expectedAsn}. */
  public static @Nonnull Matcher<EigrpProcess> hasAsn(@Nullable Long expectedAsn) {
    return new HasAsn(equalTo(expectedAsn));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpProcess}'s asn.
   */
  public static @Nonnull Matcher<EigrpProcess> hasAsn(@Nonnull Matcher<? super Long> subMatcher) {
    return new HasAsn(subMatcher);
  }

  /** Provides a matcher that matches if the {@link EigrpProcess}'s mode is {@code expectedMode}. */
  public static @Nonnull Matcher<EigrpProcess> hasMode(@Nullable EigrpProcessMode expectedMode) {
    return new HasAsn(equalTo(expectedMode));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpProcess}'s mode.
   */
  public static @Nonnull Matcher<EigrpProcess> hasMode(
      @Nonnull Matcher<? super EigrpProcessMode> subMatcher) {
    return new HasMode(subMatcher);
  }
}
