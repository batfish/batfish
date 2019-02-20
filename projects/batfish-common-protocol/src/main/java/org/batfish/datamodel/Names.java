package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Pattern;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Provides helper methods to auto-generate structure names and to check the validity of names of
 * different types of entities.
 *
 * <p>If the regex of any of the names here is changed, ensure that corresponding grammar rules in
 * package org.batfish.specifier.parboiled.CommonParser are also updated.
 */
@ParametersAreNonnullByDefault
public final class Names {

  /**
   * Valid names for reference library objects must start with a letter or underscore, and only
   * contain {-,\w} ( i.e., [-a-zA-Z_0-9])
   */
  public static final String REFERENCE_OBJECT_NAME_REGEX = "[a-zA-Z_][-\\w]*";

  private static final Pattern _REFERENCE_OBJECT_NAME_PATTERN =
      Pattern.compile(REFERENCE_OBJECT_NAME_REGEX);

  private Names() {} // prevent instantiation by default.

  public static void checkValidReferenceObjectName(String name, String objectType) {
    checkArgument(
        _REFERENCE_OBJECT_NAME_PATTERN.matcher(name).matches(),
        "Invalid %s name '%s'. Valid names begin with the alphabetic letters or underscore and can additionally contain digits and dashes.",
        objectType,
        name);
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
