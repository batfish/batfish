package org.batfish.job;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.vendor.VendorStructureId;

/**
 * Erases invalid {@link VendorStructureId}s.
 *
 * <p>{@link VendorStructureId}s are considered invalid if they do not point to a defined structure.
 */
public final class InvalidVendorStructureIdEraser
    implements GenericAclLineVisitor<AclLine>, GenericAclLineMatchExprVisitor<AclLineMatchExpr> {

  // TODO visit IpSpaceMetadata as well

  public InvalidVendorStructureIdEraser(
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
          definedStructures) {
    _definedStructures = definedStructures;
  }

  /**
   * Returns a boolean indicating if the specified {@link VendorStructureId} points to a defined
   * structure.
   */
  private boolean isVendorStructureIdValid(VendorStructureId vendorStructureId) {
    return isVendorStructureIdValid(vendorStructureId, _definedStructures);
  }

  /**
   * Returns a boolean indicating if the specified {@link VendorStructureId} points to a defined
   * structure.
   */
  @VisibleForTesting
  static boolean isVendorStructureIdValid(
      VendorStructureId vendorStructureId,
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
          definedStructures) {
    return definedStructures
        .getOrDefault(vendorStructureId.getFilename(), ImmutableSortedMap.of())
        .getOrDefault(vendorStructureId.getStructureType(), ImmutableSortedMap.of())
        .containsKey(vendorStructureId.getStructureName());
  }

  /**
   * Returns the specified {@link VendorStructureId} if it is valid. Otherwise, returns {@code
   * null}.
   */
  @Nullable
  private VendorStructureId eraseInvalid(VendorStructureId vendorStructureId) {
    return isVendorStructureIdValid(vendorStructureId) ? vendorStructureId : null;
  }

  /**
   * Return a modified version of the input {@link TraceElement} with all invalid {@link
   * org.batfish.vendor.VendorStructureId}s erased.
   *
   * <p>{@link org.batfish.datamodel.TraceElement.LinkFragment}s with invalid {@link
   * org.batfish.vendor.VendorStructureId}s are converted to flat, {@link
   * org.batfish.datamodel.TraceElement.TextFragment}s.
   */
  @VisibleForTesting
  TraceElement eraseInvalid(TraceElement traceElement) {
    TraceElement.Builder builder = TraceElement.builder();
    for (TraceElement.Fragment f : traceElement.getFragments()) {
      if (f instanceof TraceElement.TextFragment) {
        builder.add(f.getText());
        continue;
      }
      assert f instanceof TraceElement.LinkFragment;
      TraceElement.LinkFragment lf = (TraceElement.LinkFragment) f;
      if (isVendorStructureIdValid(lf.getVendorStructureId())) {
        builder.add(lf);
      } else {
        // Convert invalid link fragment to text fragment, to preserve useful text
        builder.add(lf.getText());
      }
    }
    return builder.build();
  }

  /**
   * Return a modified version of the provided {@link IpAccessList}, with each constituent {@link
   * AclLine} visited.
   */
  public IpAccessList visit(IpAccessList acl) {
    return acl.toBuilder()
        .setLines(acl.getLines().stream().map(this::visit).collect(Collectors.toList()))
        .build();
  }

  @Override
  public AclLine visitAclAclLine(AclAclLine aclAclLine) {
    TraceElement te =
        aclAclLine.getTraceElement() == null ? null : eraseInvalid(aclAclLine.getTraceElement());
    VendorStructureId vendorStructureId =
        aclAclLine.getVendorStructureId().map(this::eraseInvalid).orElse(null);
    return new AclAclLine(aclAclLine.getName(), aclAclLine.getAclName(), te, vendorStructureId);
  }

  @Override
  public AclLine visitExprAclLine(ExprAclLine exprAclLine) {
    TraceElement te =
        exprAclLine.getTraceElement() == null ? null : eraseInvalid(exprAclLine.getTraceElement());
    VendorStructureId vendorStructureId =
        exprAclLine.getVendorStructureId().map(this::eraseInvalid).orElse(null);
    return new ExprAclLine(
        exprAclLine.getAction(),
        visit(exprAclLine.getMatchCondition()),
        exprAclLine.getName(),
        te,
        vendorStructureId);
  }

  @Override
  public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    TraceElement te =
        andMatchExpr.getTraceElement() == null
            ? null
            : eraseInvalid(andMatchExpr.getTraceElement());
    return new AndMatchExpr(
        andMatchExpr.getConjuncts().stream()
            .map(this::visit)
            .collect(ImmutableList.toImmutableList()),
        te);
  }

  @Override
  public AclLineMatchExpr visitDeniedByAcl(DeniedByAcl deniedByAcl) {
    TraceElement te =
        deniedByAcl.getTraceElement() == null ? null : eraseInvalid(deniedByAcl.getTraceElement());
    return new DeniedByAcl(deniedByAcl.getAclName(), te);
  }

  @Override
  public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
    TraceElement te =
        falseExpr.getTraceElement() == null ? null : eraseInvalid(falseExpr.getTraceElement());
    return new FalseExpr(te);
  }

  @Override
  public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    TraceElement te =
        matchHeaderSpace.getTraceElement() == null
            ? null
            : eraseInvalid(matchHeaderSpace.getTraceElement());
    return new MatchHeaderSpace(matchHeaderSpace.getHeaderspace(), te);
  }

  @Override
  public AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    TraceElement te =
        matchSrcInterface.getTraceElement() == null
            ? null
            : eraseInvalid(matchSrcInterface.getTraceElement());
    return new MatchSrcInterface(matchSrcInterface.getSrcInterfaces(), te);
  }

  @Override
  public AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    TraceElement te =
        notMatchExpr.getTraceElement() == null
            ? null
            : eraseInvalid(notMatchExpr.getTraceElement());
    return new NotMatchExpr(visit(notMatchExpr.getOperand()), te);
  }

  @Override
  public AclLineMatchExpr visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    assert originatingFromDevice.getTraceElement() == null;
    return OriginatingFromDevice.INSTANCE;
  }

  @Override
  public AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    TraceElement te =
        orMatchExpr.getTraceElement() == null ? null : eraseInvalid(orMatchExpr.getTraceElement());
    return new OrMatchExpr(
        orMatchExpr.getDisjuncts().stream()
            .map(this::visit)
            .collect(ImmutableList.toImmutableList()),
        te);
  }

  @Override
  public AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    TraceElement te =
        permittedByAcl.getTraceElement() == null
            ? null
            : eraseInvalid(permittedByAcl.getTraceElement());
    return new PermittedByAcl(permittedByAcl.getAclName(), te);
  }

  @Override
  public AclLineMatchExpr visitTrueExpr(TrueExpr trueExpr) {
    TraceElement te =
        trueExpr.getTraceElement() == null ? null : eraseInvalid(trueExpr.getTraceElement());
    return new TrueExpr(te);
  }

  /**
   * All known structure definitions. These are used to determine if a {@link VendorStructureId} is
   * valid (points to a defined structure).
   */
  @Nonnull
  private final SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
      _definedStructures;
}
