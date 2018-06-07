package org.batfish.specifier;

import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;

public interface NodeSpecifierFactory {
  static NodeSpecifierFactory load(@Nonnull String name) {
    for (NodeSpecifierFactory factory : ServiceLoader.load(NodeSpecifierFactory.class)) {
      if (factory.getName().equals(name)) {
        return factory;
      }
    }

    throw new BatfishException("Could not find NodeSpecifierFactory with name " + name);
  }

  /** @return The name of the factory, used by load to find it. */
  String getName();

  /** The NodeSpecifier factory method. Input types vary by factory. */
  NodeSpecifier buildNodeSpecifier(Object input);
}
