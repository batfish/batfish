package org.batfish.question;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.SpecifierContext;

/** A set of parameters for ACL filter analysis that uses high-level specifiers. */
public final class ReachFilterParameters {
  private final @Nonnull IpSpaceSpecifier _destinationIpSpaceSpecifier;
  private final @Nonnull IpSpaceSpecifier _sourceIpSpaceSpecifier;
  private final @Nonnull HeaderSpace _headerSpace;

  private ReachFilterParameters(
      @Nonnull IpSpaceSpecifier destinationIpSpaceSpecifier,
      @Nonnull IpSpaceSpecifier sourceIpSpaceSpecifier,
      @Nonnull HeaderSpace headerSpace) {
    _destinationIpSpaceSpecifier = destinationIpSpaceSpecifier;
    _sourceIpSpaceSpecifier = sourceIpSpaceSpecifier;
    _headerSpace = headerSpace;
  }

  @Nonnull
  public IpSpaceSpecifier getDestinationIpSpaceSpecifier() {
    return _destinationIpSpaceSpecifier;
  }

  @Nonnull
  public IpSpaceSpecifier getSourceIpSpaceSpecifier() {
    return _sourceIpSpaceSpecifier;
  }

  /** Resolve all parameters and update the underlying headerspace. */
  public HeaderSpace resolveHeaderspace(SpecifierContext ctx) {
    return _headerSpace
        .toBuilder()
        .setSrcIps(resolveIpSpaceSpecifier(_sourceIpSpaceSpecifier, ctx))
        .setDstIps(resolveIpSpaceSpecifier(_destinationIpSpaceSpecifier, ctx))
        .build();
  }

  @Nonnull
  private static IpSpace resolveIpSpaceSpecifier(IpSpaceSpecifier specifier, SpecifierContext ctx) {
    return firstNonNull(
        AclIpSpace.union(
            specifier
                .resolve(ImmutableSet.of(), ctx)
                .getEntries()
                .stream()
                .map(Entry::getIpSpace)
                .collect(ImmutableList.toImmutableList())),
        EmptyIpSpace.INSTANCE);
  }

  public static final class Builder {
    private IpSpaceSpecifier _destinationIpSpaceSpecifier;
    private IpSpaceSpecifier _sourceIpSpaceSpecifier;
    private HeaderSpace _headerSpace;

    private Builder() {}

    public Builder setDestinationIpSpaceSpecifier(
        @Nonnull IpSpaceSpecifier destinationIpSpaceSpecifier) {
      _destinationIpSpaceSpecifier = destinationIpSpaceSpecifier;
      return this;
    }

    public Builder setSourceIpSpaceSpecifier(@Nonnull IpSpaceSpecifier sourceIpSpaceSpecifier) {
      _sourceIpSpaceSpecifier = sourceIpSpaceSpecifier;
      return this;
    }

    public Builder setHeaderSpace(@Nonnull HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
      return this;
    }

    public ReachFilterParameters build() {
      return new ReachFilterParameters(
          requireNonNull(_destinationIpSpaceSpecifier),
          requireNonNull(_sourceIpSpaceSpecifier),
          requireNonNull(_headerSpace));
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
