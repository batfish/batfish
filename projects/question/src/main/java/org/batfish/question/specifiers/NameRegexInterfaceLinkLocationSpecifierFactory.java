package org.batfish.question.specifiers;

import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationSpecifierFactory;
import org.batfish.specifier.NameRegexInterfaceLinkLocationSpecifier;

public class NameRegexInterfaceLinkLocationSpecifierFactory implements LocationSpecifierFactory {
  public static final String NAME =
      NameRegexInterfaceLinkLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Object input) {
    Preconditions.checkArgument(input instanceof String, "String input required for " + NAME);
    return new NameRegexInterfaceLinkLocationSpecifier(Pattern.compile((String) input));
  }
}
