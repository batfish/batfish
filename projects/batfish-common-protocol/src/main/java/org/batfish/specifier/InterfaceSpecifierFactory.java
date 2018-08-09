package org.batfish.specifier;

import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;

public interface InterfaceSpecifierFactory {
  static InterfaceSpecifierFactory load(@Nonnull String name) {
    for (InterfaceSpecifierFactory factory : ServiceLoader.load(InterfaceSpecifierFactory.class)) {
      if (factory.getName().equals(name)) {
        return factory;
      }
    }

    throw new BatfishException("Could not find InterfaceSpecifierFactory with name " + name);
  }

  /** @return The name of the factory, used by load to find it. */
  String getName();

  /** The InterfaceSpecifier factory method. Input types vary by factory. */
  InterfaceSpecifier buildInterfaceSpecifier(Object input);
}
