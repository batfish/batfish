package org.batfish.specifier.parboiled;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.applications.NamedApplication;

/**
 * Constants and helpers shared by the specifier autocompletion code. Formerly a parboiled {@code
 * BaseParser}; the grammar now lives in the ANTLR {@code SpecifierParser}, so this retains only the
 * grammar-independent pieces that other classes still use.
 */
@ParametersAreNonnullByDefault
public final class CommonParser {

  private CommonParser() {}

  // Characters we use for different set operators.
  static final String SET_OP_DIFFERENCE = "\\";
  static final String SET_OP_INTERSECTION = "&";
  static final String SET_OP_UNION = ",";

  /** The uppercased names of the applications recognized by the application specifier. */
  public static final Set<String> namedApplications =
      Arrays.stream(NamedApplication.values())
          .map(Object::toString)
          .map(String::toUpperCase)
          .collect(ImmutableSet.toImmutableSet());

  /**
   * Whether this anchor type supports escaped names. Autocomplete suggestions for these types are
   * escaped when they contain special characters.
   */
  static boolean isEscapableNameAnchor(Anchor.Type anchorType) {
    switch (anchorType) {
      case ADDRESS_GROUP_NAME:
      case FILTER_NAME:
      case INTERFACE_GROUP_NAME:
      case INTERFACE_NAME:
      case NODE_NAME:
      case NODE_ROLE_NAME:
      case NODE_ROLE_DIMENSION_NAME:
      case REFERENCE_BOOK_NAME:
      case ROUTING_POLICY_NAME:
      case VRF_NAME:
      case ZONE_NAME:
        return true;
      default:
        return false;
    }
  }
}
