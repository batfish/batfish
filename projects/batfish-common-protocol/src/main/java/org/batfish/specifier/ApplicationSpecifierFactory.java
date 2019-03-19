package org.batfish.specifier;

import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

public interface ApplicationSpecifierFactory {
  static ApplicationSpecifierFactory load(@Nonnull String name) {
    for (ApplicationSpecifierFactory factory :
        ServiceLoader.load(ApplicationSpecifierFactory.class)) {
      if (factory.getName().equals(name)) {
        return factory;
      }
    }

    throw new BatfishException("Could not find ApplicationSpecifierFactory with name " + name);
  }

  /** @return The name of the factory, used by load to find it. */
  String getName();

  /** The ApplicationSpecifier factory method. Input types vary by factory. */
  ApplicationSpecifier buildApplicationSpecifier(@Nullable Object input);
}
