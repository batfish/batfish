package org.batfish.specifier;

/**
 * Used by {@link org.batfish.datamodel.questions.Question}s to build {@link IpSpaceSpecifier}
 * objects. The factory can be responsible for parsing and validating the fields from question
 * input.
 */
public interface IpSpaceSpecifierFactory {
  String getName();

  /**
   * Construct an {@link IpSpaceSpecifier} object for the supplied input. The required input type
   * and format varies among {@link IpSpaceSpecifier} and {@link IpSpaceSpecifierFactory} types.
   *
   * @param input User input of {@link IpSpaceSpecifier} fields to be parsed and/or validated.
   * @return The constructed {@link IpSpaceSpecifier}.
   */
  IpSpaceSpecifier buildIpSpaceSpecifier(Object input);
}
