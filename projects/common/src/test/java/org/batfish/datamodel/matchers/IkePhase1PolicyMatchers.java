package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IkePhase1PolicyMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IkE Phase 1
   * Policy's IKE Phase 1 keys
   */
  public static Matcher<IkePhase1Policy> hasIkePhase1Key(
      @Nonnull Matcher<? super IkePhase1Key> subMatcher) {
    return new HasIkePhase1Key(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IkE Phase 1
   * Policy's Remote Identity
   */
  public static Matcher<IkePhase1Policy> hasRemoteIdentity(
      @Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasRemoteIdentity(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IkE Phase 1
   * Policy's Self Identity
   */
  public static Matcher<IkePhase1Policy> hasSelfIdentity(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasSelfIdentity(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IkE Phase 1
   * Policy's Local Interface
   */
  public static Matcher<IkePhase1Policy> hasLocalInterface(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasLocalInterface(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IKE Phase 1
   * Policy's IKE Phase 1 proposals
   */
  public static Matcher<IkePhase1Policy> hasIkePhase1Proposals(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasIkePhase1Proposals(subMatcher);
  }

  private IkePhase1PolicyMatchers() {}

  private static final class HasIkePhase1Key extends FeatureMatcher<IkePhase1Policy, IkePhase1Key> {
    HasIkePhase1Key(@Nonnull Matcher<? super IkePhase1Key> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with IkePhase1Key:", "IkePhase1Key");
    }

    @Override
    protected IkePhase1Key featureValueOf(IkePhase1Policy actual) {
      return actual.getIkePhase1Key();
    }
  }

  private static final class HasRemoteIdentity extends FeatureMatcher<IkePhase1Policy, IpSpace> {
    HasRemoteIdentity(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with RemoteIdentity:", "RemoteIdentity");
    }

    @Override
    protected IpSpace featureValueOf(IkePhase1Policy actual) {
      return actual.getRemoteIdentity();
    }
  }

  private static final class HasSelfIdentity extends FeatureMatcher<IkePhase1Policy, Ip> {
    HasSelfIdentity(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with SelfIdentity:", "SelfIdentity");
    }

    @Override
    protected Ip featureValueOf(IkePhase1Policy actual) {
      return actual.getSelfIdentity();
    }
  }

  private static final class HasLocalInterface extends FeatureMatcher<IkePhase1Policy, String> {
    HasLocalInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with LocalInterface:", "LocalInterface");
    }

    @Override
    protected String featureValueOf(IkePhase1Policy actual) {
      return actual.getLocalInterface();
    }
  }

  private static final class HasIkePhase1Proposals
      extends FeatureMatcher<IkePhase1Policy, List<String>> {
    HasIkePhase1Proposals(@Nonnull Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "An IKE phase 1 policy with IkePhase1Proposals:", "IkePhase1Proposals");
    }

    @Override
    protected List<String> featureValueOf(IkePhase1Policy actual) {
      return actual.getIkePhase1Proposals();
    }
  }
}
