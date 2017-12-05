package org.batfish.bdp;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class BgpAdvertisementMatchUtils {

  static final Matcher<BgpAdvertisement> hasDestinationIp(Ip expectedDestinationIp) {
    return new HasDestinationIp(expectedDestinationIp);
  }

  static final Matcher<BgpAdvertisement> hasSourceIp(Ip expectedSourceIp) {
    return new HasSourceIp(expectedSourceIp);
  }

  static final Matcher<BgpAdvertisement> hasNetwork(Prefix expectedNetwork) {
    return new HasNetwork(expectedNetwork);
  }

  static final Matcher<BgpAdvertisement> hasOriginatorIp(Ip expectedOriginatorIp) {
    return new HasOriginatorIp(expectedOriginatorIp);
  }

  static final Matcher<BgpAdvertisement> hasType(
      BgpAdvertisementType expectedBgpAdvertisementType) {
    return new HasType(expectedBgpAdvertisementType);
  }

  private static final class HasDestinationIp extends FeatureMatcher<BgpAdvertisement, Ip> {

    private HasDestinationIp(@Nonnull Ip destinationIp) {
      super(equalTo(destinationIp), "destinationIp", "destinationIp");
    }

    @Override
    protected Ip featureValueOf(BgpAdvertisement actual) {
      return actual.getDstIp();
    }
  }

  private static final class HasType
      extends FeatureMatcher<BgpAdvertisement, BgpAdvertisementType> {

    private HasType(@Nonnull BgpAdvertisementType bgpAdvertisementType) {
      super(equalTo(bgpAdvertisementType), "bgpAdvertisementType", "bgpAdvertisementType");
    }

    @Override
    protected BgpAdvertisementType featureValueOf(BgpAdvertisement actual) {
      return actual.getType();
    }
  }

  private static final class HasNetwork extends FeatureMatcher<BgpAdvertisement, Prefix> {

    private HasNetwork(@Nonnull Prefix network) {
      super(equalTo(network), "network", "network");
    }

    @Override
    protected Prefix featureValueOf(BgpAdvertisement actual) {
      return actual.getNetwork();
    }
  }

  private static final class HasOriginatorIp extends FeatureMatcher<BgpAdvertisement, Ip> {

    private HasOriginatorIp(@Nonnull Ip originatorIp) {
      super(equalTo(originatorIp), "originatorIp", "originatorIp");
    }

    @Override
    protected Ip featureValueOf(BgpAdvertisement actual) {
      return actual.getOriginatorIp();
    }
  }

  private static final class HasSourceIp extends FeatureMatcher<BgpAdvertisement, Ip> {

    private HasSourceIp(@Nonnull Ip sourceIp) {
      super(equalTo(sourceIp), "sourceIp", "sourceIp");
    }

    @Override
    protected Ip featureValueOf(BgpAdvertisement actual) {
      return actual.getSrcIp();
    }
  }
}
