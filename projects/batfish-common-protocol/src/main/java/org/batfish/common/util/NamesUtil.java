package org.batfish.common.util;

import static com.google.common.base.Preconditions.checkArgument;

import org.batfish.specifier.parboiled.ParboiledNameUtils;

public final class NamesUtil {

  public static void checkValidReferenceObjectName(String name, String objectType) {
    checkArgument(
        ParboiledNameUtils.isValidReferenceObjectName(name),
        "Invalid %s name '%s'. Valid names begin with the alphabetic letters or underscore and can additionally contain digits and dashes.",
        objectType,
        name);
  }
}
