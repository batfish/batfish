package org.batfish.specifier;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;

@AutoService(LocationSpecifierFactory.class)
public final class AllInterfacesLocationSpecifierFactory implements LocationSpecifierFactory {
  public static final String NAME = AllInterfacesLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(@Nullable Object input) {
    return AllInterfacesLocationSpecifier.INSTANCE;
  }
}
