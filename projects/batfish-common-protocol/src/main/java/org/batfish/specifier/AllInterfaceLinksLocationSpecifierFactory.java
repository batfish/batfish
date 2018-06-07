package org.batfish.specifier;

import com.google.auto.service.AutoService;

@AutoService(LocationSpecifierFactory.class)
public final class AllInterfaceLinksLocationSpecifierFactory implements LocationSpecifierFactory {
  public static final String NAME = AllInterfaceLinksLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Object input) {
    return AllInterfaceLinksLocationSpecifier.INSTANCE;
  }
}
