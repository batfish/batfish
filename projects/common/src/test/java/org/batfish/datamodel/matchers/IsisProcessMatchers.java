package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.isis.IsisLevelSettings;
import org.batfish.datamodel.isis.IsisProcess;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IsisProcessMatchers {
  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s level1.
   */
  public static @Nonnull Matcher<IsisProcess> hasLevel1(
      @Nonnull Matcher<? super IsisLevelSettings> subMatcher) {
    return new HasLevel1(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s level2.
   */
  public static @Nonnull Matcher<IsisProcess> hasLevel2(
      @Nonnull Matcher<? super IsisLevelSettings> subMatcher) {
    return new HasLevel2(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link IsisProcess}'s overload is {@code
   * expectedOverload}.
   */
  public static @Nonnull Matcher<IsisProcess> hasOverload(boolean expectedOverload) {
    return new HasOverload(equalTo(expectedOverload));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s overload.
   */
  public static @Nonnull Matcher<IsisProcess> hasOverload(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasOverload(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link IsisProcess}'s referenceBandwidth is {@code
   * expectedReferenceBandwidth}.
   */
  public static @Nonnull Matcher<IsisProcess> hasReferenceBandwidth(
      @Nullable Double expectedReferenceBandwidth) {
    return new HasReferenceBandwidth(equalTo(expectedReferenceBandwidth));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s referenceBandwidth.
   */
  public static @Nonnull Matcher<IsisProcess> hasReferenceBandwidth(
      @Nonnull Matcher<? super Double> subMatcher) {
    return new HasReferenceBandwidth(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s netAddress.
   */
  public static @Nonnull Matcher<IsisProcess> hasNetAddress(
      @Nonnull Matcher<? super IsoAddress> subMatcher) {
    return new HasNetAddress(subMatcher);
  }

  private IsisProcessMatchers() {}

  private static final class HasLevel1 extends FeatureMatcher<IsisProcess, IsisLevelSettings> {
    HasLevel1(@Nonnull Matcher<? super IsisLevelSettings> subMatcher) {
      super(subMatcher, "An IsisProcess with level1:", "level1");
    }

    @Override
    protected IsisLevelSettings featureValueOf(IsisProcess actual) {
      return actual.getLevel1();
    }
  }

  private static final class HasLevel2 extends FeatureMatcher<IsisProcess, IsisLevelSettings> {
    HasLevel2(@Nonnull Matcher<? super IsisLevelSettings> subMatcher) {
      super(subMatcher, "An IsisProcess with level2:", "level2");
    }

    @Override
    protected IsisLevelSettings featureValueOf(IsisProcess actual) {
      return actual.getLevel2();
    }
  }

  private static final class HasNetAddress extends FeatureMatcher<IsisProcess, IsoAddress> {
    HasNetAddress(@Nonnull Matcher<? super IsoAddress> subMatcher) {
      super(subMatcher, "An IsisProcess with netAddress:", "netAddress");
    }

    @Override
    protected IsoAddress featureValueOf(IsisProcess actual) {
      return actual.getNetAddress();
    }
  }

  private static final class HasReferenceBandwidth extends FeatureMatcher<IsisProcess, Double> {
    HasReferenceBandwidth(@Nonnull Matcher<? super Double> subMatcher) {
      super(subMatcher, "An IsisProcess with referenceBandwidth:", "referenceBandwidth");
    }

    @Override
    protected Double featureValueOf(IsisProcess actual) {
      return actual.getReferenceBandwidth();
    }
  }

  private static final class HasOverload extends FeatureMatcher<IsisProcess, Boolean> {
    HasOverload(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An IsisProcess with overload:", "overload");
    }

    @Override
    protected Boolean featureValueOf(IsisProcess actual) {
      return actual.getOverload();
    }
  }
}
