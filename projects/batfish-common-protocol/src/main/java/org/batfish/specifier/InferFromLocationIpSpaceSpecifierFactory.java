package org.batfish.specifier;

import com.google.auto.service.AutoService;

@AutoService(IpSpaceSpecifierFactory.class)
public class InferFromLocationIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(Object input) {
    return InferFromLocationIpSpaceSpecifier.INSTANCE;
  }
}
