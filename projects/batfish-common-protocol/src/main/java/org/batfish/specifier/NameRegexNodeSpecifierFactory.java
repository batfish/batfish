package org.batfish.specifier;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import java.util.regex.Pattern;

@AutoService(NodeSpecifierFactory.class)
public class NameRegexNodeSpecifierFactory implements NodeSpecifierFactory {
  public static final String NAME = NameRegexNodeSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public NodeSpecifier buildNodeSpecifier(Object input) {
    Preconditions.checkArgument(input instanceof String, "String input required for " + NAME);
    return new NameRegexNodeSpecifier(Pattern.compile((String) input));
  }
}
