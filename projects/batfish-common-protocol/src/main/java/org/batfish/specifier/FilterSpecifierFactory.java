package org.batfish.specifier;

import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

public interface FilterSpecifierFactory {
  static FilterSpecifierFactory load(@Nonnull String name) {
    for (FilterSpecifierFactory factory : ServiceLoader.load(FilterSpecifierFactory.class)) {
      if (factory.getName().equals(name)) {
        return factory;
      }
    }

    throw new BatfishException("Could not find FilterSpecifierFactory with name " + name);
  }

  /** @return The name of the factory, used by load to find it. */
  String getName();

  /** The FilterSpecifier factory method. Input types vary by factory. */
  FilterSpecifier buildFilterSpecifier(@Nullable Object input);
}
