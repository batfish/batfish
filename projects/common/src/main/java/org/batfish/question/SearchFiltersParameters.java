package org.batfish.question;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceAssignmentSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.SpecifierContext;

/** A set of parameters for ACL filter analysis that uses high-level specifiers. */
public final class SearchFiltersParameters {
  private final @Nonnull IpSpaceAssignmentSpecifier _destinationIpSpaceSpecifier;
  private final @Nonnull AclLineMatchExpr _headerSpaceExpr;
  private final @Nonnull LocationSpecifier _startLocationSpecifier;
  private final @Nonnull IpSpaceAssignmentSpecifier _sourceIpSpaceSpecifier;
  private final boolean _complementHeaderSpace;

  private SearchFiltersParameters(
      @Nonnull IpSpaceAssignmentSpecifier destinationIpSpaceSpecifier,
      @Nonnull LocationSpecifier startLocationSpecifier,
      @Nonnull IpSpaceAssignmentSpecifier sourceIpSpaceSpecifier,
      @Nonnull AclLineMatchExpr headerSpaceExpr,
      boolean complementHeaderSpace) {
    _destinationIpSpaceSpecifier = destinationIpSpaceSpecifier;
    _startLocationSpecifier = startLocationSpecifier;
    _sourceIpSpaceSpecifier = sourceIpSpaceSpecifier;
    _headerSpaceExpr = headerSpaceExpr;
    _complementHeaderSpace = complementHeaderSpace;
  }

  public @Nonnull IpSpaceAssignmentSpecifier getDestinationIpSpaceSpecifier() {
    return _destinationIpSpaceSpecifier;
  }

  public @Nonnull LocationSpecifier getStartLocationSpecifier() {
    return _startLocationSpecifier;
  }

  public @Nonnull IpSpaceAssignmentSpecifier getSourceIpSpaceSpecifier() {
    return _sourceIpSpaceSpecifier;
  }

  /** Resolve all parameters and update the underlying headerspace. */
  public AclLineMatchExpr resolveHeaderspace(SpecifierContext ctx) {
    AclLineMatchExpr srcExpr = matchSrc(resolveIpSpaceSpecifier(_sourceIpSpaceSpecifier, ctx));
    AclLineMatchExpr dstExpr = matchDst(resolveIpSpaceSpecifier(_destinationIpSpaceSpecifier, ctx));

    return _complementHeaderSpace
        ? not(and(_headerSpaceExpr, srcExpr, dstExpr))
        : and(_headerSpaceExpr, srcExpr, dstExpr);
  }

  private static @Nonnull IpSpace resolveIpSpaceSpecifier(
      IpSpaceAssignmentSpecifier specifier, SpecifierContext ctx) {
    return firstNonNull(
        AclIpSpace.union(
            specifier.resolve(ImmutableSet.of(), ctx).getEntries().stream()
                .map(Entry::getIpSpace)
                .collect(ImmutableList.toImmutableList())),
        EmptyIpSpace.INSTANCE);
  }

  public Builder toBuilder() {
    return new Builder()
        .setDestinationIpSpaceSpecifier(_destinationIpSpaceSpecifier)
        .setHeaderSpaceExpr(_headerSpaceExpr)
        .setStartLocationSpecifier(_startLocationSpecifier)
        .setSourceIpSpaceSpecifier(_sourceIpSpaceSpecifier)
        .setComplementHeaderSpace(_complementHeaderSpace);
  }

  public static final class Builder {
    private IpSpaceAssignmentSpecifier _destinationIpSpaceSpecifier;
    private AclLineMatchExpr _headerSpaceExpr;
    private LocationSpecifier _startLocationSpecifier;
    private IpSpaceAssignmentSpecifier _sourceIpSpaceSpecifier;
    private boolean _complementHeaderSpace;

    private Builder() {}

    public Builder setComplementHeaderSpace(boolean complementHeaderSpace) {
      _complementHeaderSpace = complementHeaderSpace;
      return this;
    }

    public Builder setDestinationIpSpaceSpecifier(
        @Nonnull IpSpaceAssignmentSpecifier destinationIpSpaceSpecifier) {
      _destinationIpSpaceSpecifier = destinationIpSpaceSpecifier;
      return this;
    }

    public Builder setHeaderSpaceExpr(@Nonnull AclLineMatchExpr headerSpaceExpr) {
      _headerSpaceExpr = headerSpaceExpr;
      return this;
    }

    public Builder setSourceIpSpaceSpecifier(
        @Nonnull IpSpaceAssignmentSpecifier sourceIpSpaceSpecifier) {
      _sourceIpSpaceSpecifier = sourceIpSpaceSpecifier;
      return this;
    }

    public SearchFiltersParameters build() {
      return new SearchFiltersParameters(
          requireNonNull(_destinationIpSpaceSpecifier),
          requireNonNull(_startLocationSpecifier),
          requireNonNull(_sourceIpSpaceSpecifier),
          requireNonNull(_headerSpaceExpr),
          _complementHeaderSpace);
    }

    public Builder setStartLocationSpecifier(@Nonnull LocationSpecifier startLocationSpecifier) {
      _startLocationSpecifier = startLocationSpecifier;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
