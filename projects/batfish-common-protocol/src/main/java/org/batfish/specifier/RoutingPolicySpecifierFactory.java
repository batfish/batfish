package org.batfish.specifier;

import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

public interface RoutingPolicySpecifierFactory {
  static RoutingPolicySpecifierFactory load(@Nonnull String name) {
    for (RoutingPolicySpecifierFactory factory :
        ServiceLoader.load(RoutingPolicySpecifierFactory.class)) {
      if (factory.getName().equals(name)) {
        return factory;
      }
    }

    throw new BatfishException("Could not find RoutingPolicySpecifierFactory with name " + name);
  }

  /** @return The name of the factory, used by load to find it. */
  String getName();

  /** The RoutingPolicySpecifier factory method. Input types vary by factory. */
  RoutingPolicySpecifier buildRoutingPolicySpecifier(@Nullable Object input);
}
