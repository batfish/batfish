package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Provides helper methods to auto-generate structure names and to check the validity of names of
 * different types of entities.
 */
@ParametersAreNonnullByDefault
public final class Names {

  /**
   * Enum for different types of names with regexes that describes valid names.
   *
   * <p>If the regex of any of the names here is changed, ensure that corresponding grammar rules in
   * org.batfish.specifier.parboiled.CommonParser are also updated
   */
  public enum Type {
    /**
     * All names inside {@link org.batfish.referencelibrary.ReferenceLibrary} and {@link
     * org.batfish.role.NodeRolesData}.
     */
    REFERENCE_OBJECT(
        "^\\p{ASCII}+$", "be non-empty and contain only (non-extended) ASCII characters"),
    /** Column names in {@link org.batfish.datamodel.table.ColumnMetadata} */
    TABLE_COLUMN(
        "^[a-zA-Z0-9_~][-/\\w\\.:~@]*$",
        "start with alphanumeric, underscore or tilde and only have  [-/.:~@]");

    Type(String regex, String explanation) {
      _regex = regex;
      _explanation = explanation;
    }

    private final String _explanation;

    private final String _regex;

    public String getExplanation() {
      return _explanation;
    }

    public String getRegex() {
      return _regex;
    }
  }

  @VisibleForTesting
  static final Map<Type, Pattern> VALID_PATTERNS =
      Arrays.stream(Type.values())
          .collect(
              ImmutableMap.toImmutableMap(Function.identity(), o -> Pattern.compile(o.getRegex())));

  private Names() {} // prevent instantiation by default.

  /**
   * Checks if {@code name} is valid for {@code type}.
   *
   * <p>{@code objectDescription} is the user-facing description of the object type.
   *
   * @throws IllegalArgumentException if the name is invalid.
   */
  public static void checkName(String name, String objectDescription, Type type) {
    checkArgument(
        VALID_PATTERNS.get(type).matcher(name).matches(),
        "Invalid %s name '%s'. Valid names must %s.",
        objectDescription,
        name,
        type.getExplanation());
  }

  public static String generatedReferenceBook(String hostname, String source) {
    return String.format("%s~on~%s", source, hostname);
  }

  /**
   * Return the Batfish canonical name for a filter between zones.
   *
   * <p>This should only be used for filters that are defined by the user but unnamed in the vendor
   * language, rather than filters that are "generated" by Batfish combining multiple user-defined
   * structures.
   */
  public static String zoneToZoneFilter(String fromZone, String toZone) {
    return String.format("zone~%s~to~zone~%s", fromZone, toZone);
  }
}
