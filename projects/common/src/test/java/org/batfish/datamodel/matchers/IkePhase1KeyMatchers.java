package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IpSpace;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IkePhase1KeyMatchers {

  /**
   * Provides a matcher that matches if the IKE Phase 1 Key's {@code keyHash} matches specified
   * {@code keyHash}
   */
  public static Matcher<IkePhase1Key> hasKeyHash(String keyHash) {
    return new HasKeyHash(equalTo(keyHash));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Key's {@code remoteIdentity} matches
   * specified {@code remoteIdentity}
   */
  public static Matcher<IkePhase1Key> hasRemoteIdentity(IpSpace remoteIdentity) {
    return new HasRemoteIdentity(equalTo(remoteIdentity));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Key's {@code keyType} matches specified
   * {@code keyType}
   */
  public static Matcher<IkePhase1Key> hasKeyType(IkeKeyType keyType) {
    return new HasKeyType(equalTo(keyType));
  }

  private IkePhase1KeyMatchers() {}

  private static final class HasKeyHash extends FeatureMatcher<IkePhase1Key, String> {
    HasKeyHash(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE phase 1 key with KeyHash:", "KeyHash");
    }

    @Override
    protected String featureValueOf(IkePhase1Key actual) {
      return actual.getKeyHash();
    }
  }

  private static final class HasKeyType extends FeatureMatcher<IkePhase1Key, IkeKeyType> {
    HasKeyType(@Nonnull Matcher<? super IkeKeyType> subMatcher) {
      super(subMatcher, "An IKE phase 1 key with KeyType:", "KeyType");
    }

    @Override
    protected IkeKeyType featureValueOf(IkePhase1Key actual) {
      return actual.getKeyType();
    }
  }

  private static final class HasRemoteIdentity extends FeatureMatcher<IkePhase1Key, IpSpace> {
    HasRemoteIdentity(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "An IKE phase 1 key with RemoteIdentity:", "RemoteIdentity");
    }

    @Override
    protected IpSpace featureValueOf(IkePhase1Key actual) {
      return actual.getRemoteIdentity();
    }
  }
}
