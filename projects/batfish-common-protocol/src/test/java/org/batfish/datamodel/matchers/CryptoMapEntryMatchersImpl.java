package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.CryptoMapEntry;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecProposal;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class CryptoMapEntryMatchersImpl {
  static final class HasAccessList extends FeatureMatcher<CryptoMapEntry, IpAccessList> {
    HasAccessList(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "A CryptoMapEntry with AccessList", "AccessList");
    }

    @Override
    protected IpAccessList featureValueOf(CryptoMapEntry actual) {
      return actual.getAccessList();
    }
  }

  static final class HasDynamic extends FeatureMatcher<CryptoMapEntry, Boolean> {
    HasDynamic(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A CryptoMapEntry with Dynamic", "Dynamic");
    }

    @Override
    protected Boolean featureValueOf(CryptoMapEntry actual) {
      return actual.getDynamic();
    }
  }

  static final class HasIkeGateway extends FeatureMatcher<CryptoMapEntry, IkeGateway> {
    HasIkeGateway(@Nonnull Matcher<? super IkeGateway> subMatcher) {
      super(subMatcher, "A CryptoMapEntry with IkeGateway", "IkeGateway");
    }

    @Override
    protected IkeGateway featureValueOf(CryptoMapEntry actual) {
      return actual.getIkeGateway();
    }
  }

  static final class HasProposals extends FeatureMatcher<CryptoMapEntry, List<IpsecProposal>> {
    HasProposals(@Nonnull Matcher<? super List<IpsecProposal>> subMatcher) {
      super(subMatcher, "A CryptoMapEntry with Proposals", "Proposals");
    }

    @Override
    protected List<IpsecProposal> featureValueOf(CryptoMapEntry actual) {
      return actual.getProposals();
    }
  }

  static final class HasPeer extends FeatureMatcher<CryptoMapEntry, Ip> {
    HasPeer(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A CryptoMapEntry with Peer", "Peer");
    }

    @Override
    protected Ip featureValueOf(CryptoMapEntry actual) {
      return actual.getPeer();
    }
  }

  static final class HasReferredDynamicMapSet extends FeatureMatcher<CryptoMapEntry, String> {
    HasReferredDynamicMapSet(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A CryptoMapEntry with ReferredDynamicMapSet", "ReferredDynamicMapSet");
    }

    @Override
    protected String featureValueOf(CryptoMapEntry actual) {
      return actual.getReferredDynamicMapSet();
    }
  }

  static final class HasSequenceNumber extends FeatureMatcher<CryptoMapEntry, Integer> {
    HasSequenceNumber(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A CryptoMapEntry with SequenceNumber", "SequenceNumber");
    }

    @Override
    protected Integer featureValueOf(CryptoMapEntry actual) {
      return actual.getSequenceNumber();
    }
  }

  static final class HasPfsKeyGroup extends FeatureMatcher<CryptoMapEntry, DiffieHellmanGroup> {
    HasPfsKeyGroup(@Nonnull Matcher<? super DiffieHellmanGroup> subMatcher) {
      super(subMatcher, "A CryptoMapEntry with PfsKeyGroup", "PfsKeyGroup");
    }

    @Override
    protected DiffieHellmanGroup featureValueOf(CryptoMapEntry actual) {
      return actual.getPfsKeyGroup();
    }
  }
}
