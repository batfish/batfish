package org.batfish.specifier;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;

/**
 * Builds a {@link LocationIpSpaceSpecifier} using {@link FlexibleLocationSpecifierFactory} to build
 * its {@link LocationSpecifier} input.
 */
@AutoService(IpSpaceSpecifierFactory.class)
public final class FlexibleLocationIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  public static final String NAME = FlexibleLocationIpSpaceSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(@Nullable Object input) {
    return new LocationIpSpaceSpecifier(
        new FlexibleLocationSpecifierFactory().buildLocationSpecifier(input));
  }
}
