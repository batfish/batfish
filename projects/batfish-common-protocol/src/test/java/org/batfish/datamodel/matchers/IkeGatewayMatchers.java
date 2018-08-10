package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePolicy;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.matchers.IkeGatewayMatchersImpl.HasAddress;
import org.batfish.datamodel.matchers.IkeGatewayMatchersImpl.HasExternalInterface;
import org.batfish.datamodel.matchers.IkeGatewayMatchersImpl.HasIkePolicy;
import org.batfish.datamodel.matchers.IkeGatewayMatchersImpl.HasLocalIp;
import org.batfish.datamodel.matchers.IkeGatewayMatchersImpl.HasName;
import org.hamcrest.Matcher;

public final class IkeGatewayMatchers {

  /**
   * Provides a matcher that matches if the IKE Gateway's value of {@code address} matches specified
   * {@code address}
   */
  public static @Nonnull HasAddress hasAddress(Ip address) {
    return new HasAddress(equalTo(address));
  }

  /**
   * Provides a matcher that matches if the IKE Gateway's value of {@code externalInterface} matches
   * specified {@code externalInterface}
   */
  public static @Nonnull HasExternalInterface hasExternalInterface(Matcher<Interface> subMatcher) {
    return new HasExternalInterface(subMatcher);
  }

  /**
   * Provides a matcher that matches if the IKE Gateway's value of {@code ikePolicy} matches
   * specified {@code ikePolicy}
   */
  public static @Nonnull HasIkePolicy hasIkePolicy(Matcher<IkePolicy> subMatcher) {
    return new HasIkePolicy(subMatcher);
  }

  /**
   * Provides a matcher that matches if the IKE Gateway's value of {@code locaLIp} matches specified
   * {@code localIp}
   */
  public static HasLocalIp hasLocalIp(Ip localIp) {
    return new HasLocalIp(equalTo(localIp));
  }

  /**
   * Provides a matcher that matches if the IKE Gateway's value of {@code name} matches specified
   * {@code name}
   */
  public static @Nonnull HasName hasName(String name) {
    return new HasName(equalTo(name));
  }

  private IkeGatewayMatchers() {}
}
