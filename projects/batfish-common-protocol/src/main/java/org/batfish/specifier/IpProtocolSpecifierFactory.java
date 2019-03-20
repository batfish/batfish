package org.batfish.specifier;

import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

public interface IpProtocolSpecifierFactory {
  static IpProtocolSpecifierFactory load(@Nonnull String name) {
    for (IpProtocolSpecifierFactory factory :
        ServiceLoader.load(IpProtocolSpecifierFactory.class)) {
      if (factory.getName().equals(name)) {
        return factory;
      }
    }

    throw new BatfishException("Could not find IpProtocolSpecifierFactory with name " + name);
  }

  /** @return The name of the factory, used by load to find it. */
  String getName();

  /** The IpProtocolSpecifier factory method. Input types vary by factory. */
  IpProtocolSpecifier buildIpProtocolSpecifier(@Nullable Object input);
}
