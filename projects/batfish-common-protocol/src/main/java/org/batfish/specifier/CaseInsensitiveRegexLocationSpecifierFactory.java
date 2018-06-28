package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Pattern;
import javax.annotation.Nullable;

public abstract class CaseInsensitiveRegexLocationSpecifierFactory
    implements LocationSpecifierFactory {
  @Override
  public final LocationSpecifier buildLocationSpecifier(@Nullable Object input) {
    checkArgument(input instanceof String, getName() + " requires input of type String");
    return buildLocationSpecifier(Pattern.compile((String) input, Pattern.CASE_INSENSITIVE));
  }

  abstract LocationSpecifier buildLocationSpecifier(Pattern pattern);
}
