package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;

/**
 * A factory for {@link ReferenceFilterGroupFilterSpecifier}. Takes input of the form "groupName,
 * bookName"
 */
@AutoService(FilterSpecifierFactory.class)
public final class ReferenceFilterGroupFilterSpecifierFactory implements FilterSpecifierFactory {
  public static final String NAME =
      ReferenceFilterGroupFilterSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public FilterSpecifier buildFilterSpecifier(@Nullable Object input) {
    checkArgument(input instanceof String, getName() + " requires input of type String");
    String[] words = ((String) input).split(",");
    checkArgument(words.length == 2, getName() + "requires two words separated by ','");
    return new ReferenceFilterGroupFilterSpecifier(words[0].trim(), words[1].trim());
  }
}
