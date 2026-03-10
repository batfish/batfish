package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IpSpace;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IkePhase1KeyMatchersImpl {

  static final class HasKeyHash extends FeatureMatcher<IkePhase1Key, String> {
    HasKeyHash(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE phase 1 key with KeyHash:", "KeyHash");
    }

    @Override
    protected String featureValueOf(IkePhase1Key actual) {
      return actual.getKeyHash();
    }
  }

  static final class HasKeyType extends FeatureMatcher<IkePhase1Key, IkeKeyType> {
    HasKeyType(@Nonnull Matcher<? super IkeKeyType> subMatcher) {
      super(subMatcher, "An IKE phase 1 key with KeyType:", "KeyType");
    }

    @Override
    protected IkeKeyType featureValueOf(IkePhase1Key actual) {
      return actual.getKeyType();
    }
  }

  static final class HasRemoteIdentity extends FeatureMatcher<IkePhase1Key, IpSpace> {
    HasRemoteIdentity(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "An IKE phase 1 key with RemoteIdentity:", "RemoteIdentity");
    }

    @Override
    protected IpSpace featureValueOf(IkePhase1Key actual) {
      return actual.getRemoteIdentity();
    }
  }
}
