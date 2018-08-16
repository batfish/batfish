package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

@AutoService(NodeSpecifierFactory.class)
public class NameRegexNodeSpecifierFactory implements NodeSpecifierFactory {
  public static final String NAME = NameRegexNodeSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public NodeSpecifier buildNodeSpecifier(@Nullable Object input) {
    Object nonNullinput = input == null ? ".*" : input;
    checkArgument(nonNullinput instanceof String, "String input required for " + NAME);
    return new NameRegexNodeSpecifier(Pattern.compile((String) nonNullinput));
  }
}
