package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.datamodel.questions.FiltersSpecifier.Type;

/**
 * A {@link FilterSpecifierFactory} for a {@link ShorthandFilterSpecifier} where the filters to
 * match are specified using the shorthands in {@link FiltersSpecifier}. Expects inputs in those
 * shorthands (e.g., outputfilteron:jk.*, name:jk.*, jk.*). See {@link FiltersSpecifier} for a
 * complete list.
 */
@AutoService(FilterSpecifierFactory.class)
public final class ShorthandFilterSpecifierFactory implements FilterSpecifierFactory {
  public static final String NAME = ShorthandFilterSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public FilterSpecifier buildFilterSpecifier(@Nullable Object input) {
    if (input == null) {
      return new ShorthandFilterSpecifier(FiltersSpecifier.ALL);
    }
    checkArgument(input instanceof String, "String input required for " + getName());
    FiltersSpecifier specifier = new FiltersSpecifier(input.toString().trim());
    checkArgument(specifier.getType() != Type.IPV6, "IPv6 is not currently supported");
    return new ShorthandFilterSpecifier(specifier);
  }
}
