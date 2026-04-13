package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class BgpAdvertisementMatchers {

  /**
   * Provides a matcher that matches when {@code expectedDestinationIp} is equal to the {@link
   * BgpAdvertisement}'s destination IP.
   */
  public static Matcher<BgpAdvertisement> hasDestinationIp(Ip expectedDestinationIp) {
    return new HasDestinationIp(equalTo(expectedDestinationIp));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * BgpAdvertisement}'s destination IP.
   */
  public static Matcher<BgpAdvertisement> hasDestinationIp(
      @Nonnull Matcher<? super Ip> subMatcher) {
    return new HasDestinationIp(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * BgpAdvertisement}'s network.
   */
  public static Matcher<BgpAdvertisement> hasNetwork(@Nonnull Matcher<? super Prefix> subMatcher) {
    return new HasNetwork(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedNetwork} is equal to the {@link
   * BgpAdvertisement}'s network.
   */
  public static Matcher<BgpAdvertisement> hasNetwork(Prefix expectedNetwork) {
    return new HasNetwork(equalTo(expectedNetwork));
  }

  /**
   * Provides a matcher that matches when {@code expectedOriginatorIp} is equal to the {@link
   * BgpAdvertisement}'s originator IP.
   */
  public static Matcher<BgpAdvertisement> hasOriginatorIp(Ip expectedOriginatorIp) {
    return new HasOriginatorIp(equalTo(expectedOriginatorIp));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * BgpAdvertisement}'s originator IP.
   */
  public static Matcher<BgpAdvertisement> hasOriginatorIp(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasOriginatorIp(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedSourceIp} is equal to the {@link
   * BgpAdvertisement}'s source IP.
   */
  public static Matcher<BgpAdvertisement> hasSourceIp(Ip expectedSourceIp) {
    return new HasSourceIp(equalTo(expectedSourceIp));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * BgpAdvertisement}'s source IP.
   */
  public static Matcher<BgpAdvertisement> hasSourceIp(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasSourceIp(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@code expectedType} is equal to the {@link
   * BgpAdvertisement}'s type.
   */
  public static Matcher<BgpAdvertisement> hasType(BgpAdvertisementType expectedType) {
    return new HasType(equalTo(expectedType));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * BgpAdvertisement}'s type.
   */
  public static Matcher<BgpAdvertisement> hasType(
      @Nonnull Matcher<? super BgpAdvertisementType> subMatcher) {
    return new HasType(subMatcher);
  }

  private BgpAdvertisementMatchers() {}

  private static final class HasDestinationIp extends FeatureMatcher<BgpAdvertisement, Ip> {
    HasDestinationIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "destinationIp", "destinationIp");
    }

    @Override
    protected Ip featureValueOf(BgpAdvertisement actual) {
      return actual.getDstIp();
    }
  }

  private static final class HasNetwork extends FeatureMatcher<BgpAdvertisement, Prefix> {
    HasNetwork(Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "network", "network");
    }

    @Override
    protected Prefix featureValueOf(BgpAdvertisement actual) {
      return actual.getNetwork();
    }
  }

  private static final class HasOriginatorIp extends FeatureMatcher<BgpAdvertisement, Ip> {
    HasOriginatorIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "originatorIp", "originatorIp");
    }

    @Override
    protected Ip featureValueOf(BgpAdvertisement actual) {
      return actual.getOriginatorIp();
    }
  }

  private static final class HasSourceIp extends FeatureMatcher<BgpAdvertisement, Ip> {
    HasSourceIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "sourceIp", "sourceIp");
    }

    @Override
    protected Ip featureValueOf(BgpAdvertisement actual) {
      return actual.getSrcIp();
    }
  }

  private static final class HasType
      extends FeatureMatcher<BgpAdvertisement, BgpAdvertisementType> {
    HasType(Matcher<? super BgpAdvertisementType> subMatcher) {
      super(subMatcher, "bgpAdvertisementType", "bgpAdvertisementType");
    }

    @Override
    protected BgpAdvertisementType featureValueOf(BgpAdvertisement actual) {
      return actual.getType();
    }
  }
}
