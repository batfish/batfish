package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.matchers.EigrpInterfaceSettingsMatchersImpl.HasAsn;
import org.batfish.datamodel.matchers.EigrpInterfaceSettingsMatchersImpl.HasEnabled;
import org.hamcrest.Matcher;

public class EigrpInterfaceSettingsMatchers {

  private EigrpInterfaceSettingsMatchers() {}

  /**
   * Provides a matcher that matches if the {@link EigrpInterfaceSettings}'s asn is {@code
   * expectedAsn}.
   */
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasAsn(@Nullable Long expectedAsn) {
    return new HasAsn(equalTo(expectedAsn));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpInterfaceSettings}'s asn.
   */
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasAsn(
      @Nonnull Matcher<? super Long> subMatcher) {
    return new HasAsn(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link EigrpInterfaceSettings}'s enabled is {@code
   * expectedEnabled}.
   */
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasEnabled(
      @Nullable Boolean expectedEnabled) {
    return new HasEnabled(equalTo(expectedEnabled));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpInterfaceSettings}'s enabled.
   */
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasEnabled(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasEnabled(subMatcher);
  }
}
