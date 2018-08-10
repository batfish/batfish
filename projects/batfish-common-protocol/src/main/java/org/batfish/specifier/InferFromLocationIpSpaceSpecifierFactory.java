package org.batfish.specifier;

import com.google.auto.service.AutoService;

@AutoService(IpSpaceSpecifierFactory.class)
public final class InferFromLocationIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  public static final String NAME = InferFromLocationIpSpaceSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(Object input) {
    return InferFromLocationIpSpaceSpecifier.INSTANCE;
  }
}
