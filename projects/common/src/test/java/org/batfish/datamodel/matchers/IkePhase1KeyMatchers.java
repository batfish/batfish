package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.matchers.IkePhase1KeyMatchersImpl.HasKeyHash;
import org.batfish.datamodel.matchers.IkePhase1KeyMatchersImpl.HasKeyType;
import org.batfish.datamodel.matchers.IkePhase1KeyMatchersImpl.HasRemoteIdentity;

public final class IkePhase1KeyMatchers {

  /**
   * Provides a matcher that matches if the IKE Phase 1 Key's {@code keyHash} matches specified
   * {@code keyHash}
   */
  public static HasKeyHash hasKeyHash(String keyHash) {
    return new HasKeyHash(equalTo(keyHash));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Key's {@code remoteIdentity} matches
   * specified {@code remoteIdentity}
   */
  public static HasRemoteIdentity hasRemoteIdentity(IpSpace remoteIdentity) {
    return new HasRemoteIdentity(equalTo(remoteIdentity));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Key's {@code keyType} matches specified
   * {@code keyType}
   */
  public static HasKeyType hasKeyType(IkeKeyType keyType) {
    return new HasKeyType(equalTo(keyType));
  }

  private IkePhase1KeyMatchers() {}
}
