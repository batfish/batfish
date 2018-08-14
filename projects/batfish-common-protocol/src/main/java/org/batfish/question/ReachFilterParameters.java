package org.batfish.question;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.SpecifierContext;

/** A set of parameters for ACL filter analysis that uses high-level specifiers. */
public class ReachFilterParameters {
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

  public IpSpaceSpecifier getSourceIpSpaceSpecifier() {
    return _sourceIpSpaceSpecifier;
  }

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  /** Resolve all parameters and update the underlying headerspace. */
  public void resolveHeaderspace(SpecifierContext ctx) {
    _headerSpace.setDstIps(
        _destinationIpSpaceSpecifier
            .resolve(ImmutableSet.of(), ctx)
            .getEntries()
            .stream()
            .map(Entry::getIpSpace)
            .findFirst()
            .orElse(EmptyIpSpace.INSTANCE));
    _headerSpace.setSrcIps(
        _sourceIpSpaceSpecifier
            .resolve(ImmutableSet.of(), ctx)
            .getEntries()
            .stream()
            .map(Entry::getIpSpace)
            .findFirst()
            .orElse(EmptyIpSpace.INSTANCE));
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
