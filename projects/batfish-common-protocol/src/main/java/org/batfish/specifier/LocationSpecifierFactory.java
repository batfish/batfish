package org.batfish.specifier;

import java.util.ServiceLoader;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

/**
 * A LocationSpecifierFactory is used by {@link org.batfish.datamodel.questions.Question}s to build
 * {@link LocationSpecifier} objects. The factory can be responsible for parsing and validating the
 * fields from question input.
 */
public interface LocationSpecifierFactory {
  static LocationSpecifierFactory load(String name) {
    for (LocationSpecifierFactory factory : ServiceLoader.load(LocationSpecifierFactory.class)) {
      if (factory.getName().equals(name)) {
        return factory;
      }
    }
    throw new BatfishException("Could not find a LocationSpecifierFactory with name " + name);
  }

  String getName();

  /**
   * Construct a {@link LocationSpecifier} object for the supplied input. The required input type
   * and format varies among {@link LocationSpecifier} and {@link LocationSpecifierFactory} types.
   *
   * @param input User input of {@link LocationSpecifier} fields to be parsed and/or validated.
   * @return The constructed {@link LocationSpecifier}.
   */
  LocationSpecifier buildLocationSpecifier(@Nullable Object input);
}
