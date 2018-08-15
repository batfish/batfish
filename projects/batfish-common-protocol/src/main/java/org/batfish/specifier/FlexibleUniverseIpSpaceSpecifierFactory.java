package org.batfish.specifier;

import com.google.auto.service.AutoService;
import org.batfish.datamodel.UniverseIpSpace;

/**
 * An {@link FlexibleIpSpaceSpecifierFactory} that returns a {@link ConstantIpSpaceSpecifier
 * constant} {@link UniverseIpSpace universe} {@link IpSpaceSpecifier} by default.
 */
@AutoService(IpSpaceSpecifierFactory.class)
public final class FlexibleUniverseIpSpaceSpecifierFactory extends FlexibleIpSpaceSpecifierFactory {
  public static final String NAME = FlexibleUniverseIpSpaceSpecifierFactory.class.getSimpleName();

  @Override
  protected IpSpaceSpecifier defaultIpSpaceSpecifier() {
    return new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
