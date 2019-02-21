package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.matchers.EigrpInterfaceSettingsMatchersImpl.HasAsn;
import org.batfish.datamodel.matchers.EigrpInterfaceSettingsMatchersImpl.HasEigrpMetric;
import org.batfish.datamodel.matchers.EigrpInterfaceSettingsMatchersImpl.HasEnabled;
import org.batfish.datamodel.matchers.EigrpInterfaceSettingsMatchersImpl.HasPassive;
import org.hamcrest.Matcher;

public class EigrpInterfaceSettingsMatchers {

  private EigrpInterfaceSettingsMatchers() {}

  /**
   * Provides a matcher that matches if the {@link EigrpInterfaceSettings}'s asn is {@code
   * expectedAsn}.
   */
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasAsn(long expectedAsn) {
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
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasEnabled(boolean expectedEnabled) {
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

  /**
   * Provides a matcher that matches if the {@link EigrpInterfaceSettings}'s metric is {@code
   * expectedMetric}.
   */
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasEigrpMetric(
      EigrpMetric expectedMetric) {
    return new HasEigrpMetric(equalTo(expectedMetric));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpInterfaceSettings}'s metric.
   */
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasEigrpMetric(
      @Nonnull Matcher<? super EigrpMetric> subMatcher) {
    return new HasEigrpMetric(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link EigrpInterfaceSettings}'s passive is {@code
   * expectedPassive}.
   */
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasPassive(boolean expectedPassive) {
    return new HasPassive(equalTo(expectedPassive));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpInterfaceSettings}'s passive.
   */
  public static @Nonnull Matcher<EigrpInterfaceSettings> hasPassive(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasPassive(subMatcher);
  }
}
