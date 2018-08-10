package org.batfish.specifier;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;

@AutoService(LocationSpecifierFactory.class)
public final class AllInterfaceLinksLocationSpecifierFactory implements LocationSpecifierFactory {
  public static final String NAME = AllInterfaceLinksLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(@Nullable Object input) {
    return AllInterfaceLinksLocationSpecifier.INSTANCE;
  }
}
