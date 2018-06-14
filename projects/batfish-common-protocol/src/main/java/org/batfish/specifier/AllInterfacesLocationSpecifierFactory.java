package org.batfish.specifier;

import com.google.auto.service.AutoService;

@AutoService(LocationSpecifierFactory.class)
public final class AllInterfacesLocationSpecifierFactory implements LocationSpecifierFactory {
  public static final String NAME = AllInterfacesLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Object input) {
    return AllInterfacesLocationSpecifier.INSTANCE;
  }
}
