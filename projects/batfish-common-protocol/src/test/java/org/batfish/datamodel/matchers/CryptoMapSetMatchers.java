package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.CryptoMapEntry;
import org.batfish.datamodel.matchers.CryptoMapSetMatchersImpl.HasCryptoMapEntries;
import org.batfish.datamodel.matchers.CryptoMapSetMatchersImpl.HasDynamic;
import org.hamcrest.Matcher;

public final class CryptoMapSetMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map set's
   * Dynamic.
   */
  public static CryptoMapSetMatchersImpl.HasDynamic hasDynamic(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasDynamic(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map set's
   * Crypto-Map entries.
   */
  public static HasCryptoMapEntries hasCryptoMapEntries(
      @Nonnull Matcher<? super List<CryptoMapEntry>> subMatcher) {
    return new HasCryptoMapEntries(subMatcher);
  }

  private CryptoMapSetMatchers() {}
}
