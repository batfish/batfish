package org.batfish.specifier;

/**
 * A LocationSpecifierFactory is used by {@link org.batfish.datamodel.questions.Question}s to build
 * {@link LocationSpecifier} objects. The factory can be responsible for parsing and validating the
 * fields from question input.
 */
public interface LocationSpecifierFactory {
  String getName();

  /**
   * Construct a {@link LocationSpecifier} object for the supplied input. The required input type
   * and format varies among {@link LocationSpecifier} and {@link LocationSpecifierFactory} types.
   *
   * @param input User input of {@link LocationSpecifier} fields to be parsed and/or validated.
   * @return The constructed {@link LocationSpecifier}.
   */
  LocationSpecifier buildLocationSpecifier(Object input);
}
