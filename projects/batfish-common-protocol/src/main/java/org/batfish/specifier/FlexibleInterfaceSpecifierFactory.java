package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.datamodel.questions.InterfacesSpecifier;

/**
 * A {@link InterfaceSpecifierFactory} that (currently) delegates to {@link
 * ShorthandInterfaceSpecifier}.
 */
@AutoService(InterfaceSpecifierFactory.class)
public class FlexibleInterfaceSpecifierFactory implements InterfaceSpecifierFactory {
  public static final String NAME = FlexibleInterfaceSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public InterfaceSpecifier buildInterfaceSpecifier(Object input) {
    if (input == null) {
      return new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    }
    checkArgument(input instanceof String, NAME + " requires String input");
    String str = ((String) input).trim();
    return new ShorthandInterfaceSpecifier(new InterfacesSpecifier(str));
  }
}
