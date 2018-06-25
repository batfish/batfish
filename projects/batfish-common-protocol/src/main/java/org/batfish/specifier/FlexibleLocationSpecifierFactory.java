package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that parses a specification from a string. The goals are to be
 * suitable for use on the command line (so a fairly terse format), while also being expressive
 * enough to express any subset of {@link Location locations}. For example, you can simply enumerate
 * a list of locations.
 *
 * <p>Recognizes the language defined by this pseudo-grammar:
 *
 * <pre>
 * Pattern      ::= [Specifier](;[Specifier])*
 * Specifier    ::= ([LocationType]:)?[Clause](,[Clause])*
 * Clause       ::= ([PropertyType]=)?[Regex]
 * PropertyType ::= node | vrf | name
 * LocationType ::= interface | interfaceLink
 * </pre>
 *
 * <p>A Pattern is a semicolon-separated list of Specifiers. Each specifier represents a set of
 * locations of a single type -- {@link InterfaceLocation} or {@link InterfaceLinkLocation}. The
 * Pattern represents the union of those sets.
 *
 * <p>A Specifier includes an optional constraint on the location type (the default is
 * "interfaceLink"), and between 1 and 3 clauses. Each clause represents a set of a locations, and
 * the specifier represents the intersection of those sets.
 *
 * <p>A clause is a regex, optionally prepended with a property type specifier followed by '='. The
 * default property type is "name".
 *
 * <p>There are three property types: the node ("node"), VRF ("vrf"), or name ("name") of the
 * interface.
 */
@AutoService(LocationSpecifierFactory.class)
public class FlexibleLocationSpecifierFactory implements LocationSpecifierFactory {
  public static final String NAME = FlexibleLocationSpecifierFactory.class.getSimpleName();

  static final String LOCATION_TYPE_INTERFACE = "interface";
  static final String LOCATION_TYPE_INTERFACE_LINK = "interfaceLink";
  static final String DEFAULT_LOCATION_TYPE = LOCATION_TYPE_INTERFACE_LINK;

  static final String PROPERTY_TYPE_NAME = "name";
  static final String PROPERTY_TYPE_NODE = "node";
  static final String PROPERTY_TYPE_VRF = "vrf";
  static final String DEFAULT_PROPERTY_TYPE = PROPERTY_TYPE_NAME;

  private static final Map<String, ClauseParser> CLAUSE_PARSERS =
      ImmutableMap.of(
          LOCATION_TYPE_INTERFACE, new InterfaceClauseParser(),
          LOCATION_TYPE_INTERFACE_LINK, new InterfaceLinkClauseParser());

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Object input) {
    if (input == null) {
      return AllInterfaceLinksLocationSpecifier.INSTANCE;
    }
    checkArgument(input instanceof String, NAME + " input must be a String");
    String str = (String) input;
    return Arrays.stream(str.split(";"))
        .map(FlexibleLocationSpecifierFactory::parseSpecifier)
        .reduce(UnionLocationSpecifier::new)
        .get(); // never empty: split never returns a zero-element array
  }

  @VisibleForTesting
  static LocationSpecifier parseSpecifier(String s) {
    String[] typeAndClauses = s.split(":");

    String locationType;
    String clauses;
    switch (typeAndClauses.length) {
      case 1:
        locationType = DEFAULT_LOCATION_TYPE;
        clauses = typeAndClauses[0];
        break;
      case 2:
        locationType = typeAndClauses[0];
        clauses = typeAndClauses[1];
        break;
      default:
        throw new IllegalArgumentException("Too many ':'s in " + s);
    }

    checkArgument(CLAUSE_PARSERS.containsKey(locationType), "Unknown type: " + locationType);
    ClauseParser clauseParser = CLAUSE_PARSERS.get(locationType);

    return Arrays.stream(clauses.split(","))
        .map(clauseParser::parse)
        .reduce(IntersectionLocationSpecifier::new)
        .get(); // never empty: split never returns a zero-element array
  }

  abstract static class ClauseParser {
    LocationSpecifier parse(String s) {
      String[] typeAndRegex = s.split("=");

      String propertyType;
      String regex;
      switch (typeAndRegex.length) {
        case 1:
          propertyType = DEFAULT_PROPERTY_TYPE;
          regex = typeAndRegex[0];
          break;
        case 2:
          propertyType = typeAndRegex[0];
          regex = typeAndRegex[1];
          break;
        default:
          throw new IllegalArgumentException("Too many '='s in " + s);
      }

      Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

      if (propertyType.equals(PROPERTY_TYPE_NAME)) {
        return nameRegex(pattern);
      }
      if (propertyType.equals(PROPERTY_TYPE_NODE)) {
        return nodeRegex(pattern);
      }
      if (propertyType.equals(PROPERTY_TYPE_VRF)) {
        return vrfRegex(pattern);
      }

      throw new IllegalArgumentException("Unknown property type: " + s);
    }

    protected abstract LocationSpecifier nameRegex(Pattern pattern);

    protected abstract LocationSpecifier nodeRegex(Pattern pattern);

    protected abstract LocationSpecifier vrfRegex(Pattern pattern);
  }

  static final class InterfaceClauseParser extends ClauseParser {
    @Override
    protected LocationSpecifier nameRegex(Pattern pattern) {
      return new NameRegexInterfaceLocationSpecifier(pattern);
    }

    @Override
    protected LocationSpecifier nodeRegex(Pattern pattern) {
      return new NodeNameRegexInterfaceLocationSpecifier(pattern);
    }

    @Override
    protected LocationSpecifier vrfRegex(Pattern pattern) {
      return new VrfNameRegexInterfaceLocationSpecifier(pattern);
    }
  }

  static final class InterfaceLinkClauseParser extends ClauseParser {
    @Override
    protected LocationSpecifier nameRegex(Pattern pattern) {
      return new NameRegexInterfaceLinkLocationSpecifier(pattern);
    }

    @Override
    protected LocationSpecifier nodeRegex(Pattern pattern) {
      return new NodeNameRegexInterfaceLinkLocationSpecifier(pattern);
    }

    @Override
    protected LocationSpecifier vrfRegex(Pattern pattern) {
      return new VrfNameRegexInterfaceLinkLocationSpecifier(pattern);
    }
  }
}
