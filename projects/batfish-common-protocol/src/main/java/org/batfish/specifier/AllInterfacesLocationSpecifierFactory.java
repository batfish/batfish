package org.batfish.specifier;

import com.google.auto.service.AutoService;

@AutoService(LocationSpecifierFactory.class)
public class AllInterfacesLocationSpecifierFactory implements LocationSpecifierFactory {
  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Object input) {
    return AllInterfacesLocationSpecifier.INSTANCE;
  }
}
