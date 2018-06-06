package org.batfish.specifier;

import com.google.auto.service.AutoService;
import org.batfish.datamodel.UniverseIpSpace;

@AutoService(IpSpaceSpecifierFactory.class)
public class ConstantUniverseIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(Object input) {
    return new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE);
  }
}
