package org.batfish.specifier;

import java.util.ServiceLoader;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

/**
 * Used by {@link org.batfish.datamodel.questions.Question}s to build {@link IpSpaceSpecifier}
 * objects. The factory can be responsible for parsing and validating the fields from question
 * input.
 */
public interface IpSpaceSpecifierFactory {
  static IpSpaceSpecifierFactory load(String name) {
    for (IpSpaceSpecifierFactory factory : ServiceLoader.load(IpSpaceSpecifierFactory.class)) {
      if (factory.getName().equals(name)) {
        return factory;
      }
    }
    throw new BatfishException("Could not find an IpSpaceSpecifierFactory with name " + name);
  }

  String getName();

  /**
   * Construct an {@link IpSpaceSpecifier} object for the supplied input. The required input type
   * and format varies among {@link IpSpaceSpecifier} and {@link IpSpaceSpecifierFactory} types.
   *
   * @param input User input of {@link IpSpaceSpecifier} fields to be parsed and/or validated.
   * @return The constructed {@link IpSpaceSpecifier}.
   */
  IpSpaceSpecifier buildIpSpaceSpecifier(@Nullable Object input);
}
