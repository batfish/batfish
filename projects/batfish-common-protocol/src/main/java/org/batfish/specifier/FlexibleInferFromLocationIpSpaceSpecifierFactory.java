package org.batfish.specifier;

import com.google.auto.service.AutoService;

/**
 * An {@link FlexibleIpSpaceSpecifierFactory} that uses {@link InferFromLocationIpSpaceSpecifier} as
 * the default.
 */
@AutoService(IpSpaceSpecifierFactory.class)
public final class FlexibleInferFromLocationIpSpaceSpecifierFactory
    extends FlexibleIpSpaceSpecifierFactory {
  public static final String NAME =
      FlexibleInferFromLocationIpSpaceSpecifierFactory.class.getSimpleName();

  @Override
  protected IpSpaceSpecifier defaultIpSpaceSpecifier() {
    return InferFromLocationIpSpaceSpecifier.INSTANCE;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
