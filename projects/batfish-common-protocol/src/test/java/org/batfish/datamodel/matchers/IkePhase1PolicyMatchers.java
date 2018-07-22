package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.matchers.IkePhase1PolicyMatchersImpl.HasIkePhase1Key;
import org.batfish.datamodel.matchers.IkePhase1PolicyMatchersImpl.HasIkePhase1Proposals;
import org.batfish.datamodel.matchers.IkePhase1PolicyMatchersImpl.HasLocalInterface;
import org.batfish.datamodel.matchers.IkePhase1PolicyMatchersImpl.HasRemoteIdentity;
import org.batfish.datamodel.matchers.IkePhase1PolicyMatchersImpl.HasSelfIdentity;
import org.hamcrest.Matcher;

public final class IkePhase1PolicyMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IkE Phase 1
   * Policy's IKE Phase 1 keys
   */
  public static HasIkePhase1Key hasIkePhase1Key(@Nonnull Matcher<? super IkePhase1Key> subMatcher) {
    return new HasIkePhase1Key(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IkE Phase 1
   * Policy's Remote Identity
   */
  public static HasRemoteIdentity hasRemoteIdentity(@Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasRemoteIdentity(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IkE Phase 1
   * Policy's Self Identity
   */
  public static HasSelfIdentity hasSelfIdentity(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasSelfIdentity(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IkE Phase 1
   * Policy's Local Interface
   */
  public static HasLocalInterface hasLocalInterface(@Nonnull Matcher<? super String> subMatcher) {
    return new HasLocalInterface(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IKE Phase 1
   * Policy's IKE Phase 1 proposals
   */
  public static HasIkePhase1Proposals hasIkePhase1Proposals(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasIkePhase1Proposals(subMatcher);
  }

  private IkePhase1PolicyMatchers() {}
}
