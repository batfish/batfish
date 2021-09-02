package org.batfish.vendor.check_point_gateway.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.NatTranslatedAddress;
import org.batfish.vendor.check_point_management.NatTranslatedService;
import org.batfish.vendor.check_point_management.NatTranslatedServiceVisitor;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.Service;
import org.batfish.vendor.check_point_management.ServiceGroup;
import org.batfish.vendor.check_point_management.ServiceTcp;
import org.batfish.vendor.check_point_management.ServiceVisitor;

public class CheckpointNatConversions {
  private static final OriginalServiceToHeaderSpaceConstraints
      ORIGINAL_SERVICE_TO_HEADER_SPACE_CONSTRAINTS = new OriginalServiceToHeaderSpaceConstraints();
  private static final TranslatedServiceToTransformationSteps
      TRANSLATED_SERVICE_TO_TRANSFORMATION_STEPS = new TranslatedServiceToTransformationSteps();

  /**
   * Restricts the given {@link HeaderSpace.Builder} to protocols/ports matching the given {@link
   * Service}.
   */
  public static void applyOriginalServiceConstraint(Service service, HeaderSpace.Builder hsb) {
    ORIGINAL_SERVICE_TO_HEADER_SPACE_CONSTRAINTS.setHeaderSpace(hsb);
    ORIGINAL_SERVICE_TO_HEADER_SPACE_CONSTRAINTS.visit(service);
    ORIGINAL_SERVICE_TO_HEADER_SPACE_CONSTRAINTS.setHeaderSpace(null);
  }

  public static @Nonnull List<TransformationStep> getServiceTransformationSteps(
      NatTranslatedService service) {
    return TRANSLATED_SERVICE_TO_TRANSFORMATION_STEPS.visit(service);
  }

  public static @Nonnull List<TransformationStep> getSourceTransformationSteps(
      NatTranslatedAddress source) {
    // TODO: Implement visitor to convert translated source to transformation steps.
    //       Will also be dependent on whether transformation is incoming or outgoing.
    return ImmutableList.of();
  }

  public static @Nonnull List<TransformationStep> getDestinationTransformationSteps(
      NatTranslatedAddress destination) {
    // TODO: Implement visitor to convert translated destination to transformation steps.
    //       Will also be dependent on whether transformation is incoming or outgoing.
    return ImmutableList.of();
  }

  /**
   * Applies a {@link Service} to its current {@link HeaderSpace.Builder}. Does not modify the
   * headerspace if the given service object is unconstrained.
   */
  private static class OriginalServiceToHeaderSpaceConstraints implements ServiceVisitor<Void> {
    private @Nullable HeaderSpace.Builder _hsb;

    private OriginalServiceToHeaderSpaceConstraints() {}

    private void setHeaderSpace(@Nullable HeaderSpace.Builder hsb) {
      _hsb = hsb;
    }

    @Override
    public Void visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
      // Does not constrain headerspace
      return null;
    }

    @Override
    public Void visitServiceGroup(ServiceGroup serviceGroup) {
      // TODO Implement
      return null;
    }

    @Override
    public Void visitServiceTcp(ServiceTcp serviceTcp) {
      // TODO Is this correct/sufficient? Does it need to modify src port?
      //      Also, need to verify that port is an integer and decide what to do if not
      assert _hsb != null;
      _hsb.setIpProtocols(IpProtocol.TCP);
      _hsb.setDstPorts(SubRange.singleton(Integer.parseInt(serviceTcp.getPort())));
      return null;
    }
  }

  // TODO Implement
  private static class TranslatedServiceToTransformationSteps
      implements NatTranslatedServiceVisitor<List<TransformationStep>> {
    private TranslatedServiceToTransformationSteps() {}

    @Override
    public List<TransformationStep> visitOriginal(Original original) {
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
      // TODO: warn
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitServiceGroup(ServiceGroup serviceGroup) {
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitServiceTcp(ServiceTcp serviceTcp) {
      return ImmutableList.of();
    }
  }
}
