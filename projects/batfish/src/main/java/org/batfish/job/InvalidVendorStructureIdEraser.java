package org.batfish.job;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSourceIp;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.references.StructureManager;
import org.batfish.vendor.VendorStructureId;

/**
 * Erases invalid {@link VendorStructureId}s.
 *
 * <p>{@link VendorStructureId}s are considered invalid if they do not point to a defined structure.
 */
@ParametersAreNonnullByDefault
public final class InvalidVendorStructureIdEraser
    implements GenericAclLineVisitor<AclLine>, GenericAclLineMatchExprVisitor<AclLineMatchExpr> {

  // TODO visit IpSpaceMetadata as well

  public InvalidVendorStructureIdEraser(String filename, StructureManager structures) {
    _filename = filename;
    _structureManager = structures;
  }

  /**
   * Returns a boolean indicating if the specified {@link VendorStructureId} points to a defined
   * structure.
   */
  private boolean isVendorStructureIdValid(VendorStructureId vendorStructureId) {
    boolean f1 = _filename.equals(vendorStructureId.getFilename());
    boolean f2 =
        _structureManager.hasDefinition(
            vendorStructureId.getStructureType(), vendorStructureId.getStructureName());
    return f1 && f2;
  }

  /**
   * Returns the specified {@link VendorStructureId} if it is valid. Otherwise, returns {@code
   * null}.
   */
  private @Nullable VendorStructureId eraseInvalid(VendorStructureId vendorStructureId) {
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
  public AclLineMatchExpr visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
    if (matchDestinationIp.getTraceElement() == null) {
      return matchDestinationIp;
    }
    TraceElement te = eraseInvalid(matchDestinationIp.getTraceElement());
    if (te.equals(matchDestinationIp.getTraceElement())) {
      return matchDestinationIp;
    }
    return AclLineMatchExprs.matchDst(matchDestinationIp.getIps(), te);
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
  public AclLineMatchExpr visitMatchSourceIp(MatchSourceIp matchSourceIp) {
    if (matchSourceIp.getTraceElement() == null) {
      return matchSourceIp;
    }
    TraceElement te = eraseInvalid(matchSourceIp.getTraceElement());
    if (te.equals(matchSourceIp.getTraceElement())) {
      return matchSourceIp;
    }
    return AclLineMatchExprs.matchSrc(matchSourceIp.getIps(), te);
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

  private final @Nonnull StructureManager _structureManager;

  private final @Nonnull String _filename;
}
