package org.batfish.vendor.check_point_gateway.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.ConcreteService;
import org.batfish.vendor.check_point_management.ConcreteSrcOrDst;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.CpmiGatewayCluster;
import org.batfish.vendor.check_point_management.Group;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.NatTranslatedService;
import org.batfish.vendor.check_point_management.NatTranslatedServiceVisitor;
import org.batfish.vendor.check_point_management.NatTranslatedSrcOrDst;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.PolicyTargets;
import org.batfish.vendor.check_point_management.Service;
import org.batfish.vendor.check_point_management.ServiceGroup;
import org.batfish.vendor.check_point_management.ServiceTcp;
import org.batfish.vendor.check_point_management.ServiceVisitor;
import org.batfish.vendor.check_point_management.SrcOrDst;
import org.batfish.vendor.check_point_management.SrcOrDstVisitor;
import org.batfish.vendor.check_point_management.UnhandledGlobal;

public class CheckpointNatConversions {
  private static final NatOriginalServiceConverter ORIGINAL_SERVICE_CONVERTER =
      new NatOriginalServiceConverter();
  private static final SrcOrDstToIpSpace SRC_OR_DST_TO_IP_SPACE = new SrcOrDstToIpSpace();
  private static final NatTranslatedServiceConverter TRANSLATED_SERVICE_CONVERTER =
      new NatTranslatedServiceConverter();

  /**
   * Restricts the given {@link HeaderSpace.Builder} to protocols/ports matching the given {@link
   * Service}.
   */
  public static void applyOriginalServiceConstraint(Service service, HeaderSpace.Builder hsb) {
    ORIGINAL_SERVICE_CONVERTER.setHeaderSpace(hsb);
    ORIGINAL_SERVICE_CONVERTER.visit(service);
    ORIGINAL_SERVICE_CONVERTER.setHeaderSpace(null);
  }

  /**
   * Restricts the given {@link HeaderSpace.Builder} to sources matching the given {@link SrcOrDst}.
   */
  public static void applyOriginalSourceConstraint(SrcOrDst source, HeaderSpace.Builder hsb) {
    SRC_OR_DST_TO_IP_SPACE.visit(source).ifPresent(hsb::setSrcIps);
  }

  /**
   * Restricts the given {@link HeaderSpace.Builder} to destinations matching the given {@link
   * SrcOrDst}.
   */
  public static void applyOriginalDestinationConstraint(
      SrcOrDst destination, HeaderSpace.Builder hsb) {
    SRC_OR_DST_TO_IP_SPACE.visit(destination).ifPresent(hsb::setDstIps);
  }

  public static @Nonnull List<TransformationStep> getServiceTransformationSteps(
      NatTranslatedService service) {
    return TRANSLATED_SERVICE_CONVERTER.visit(service);
  }

  public static @Nonnull List<TransformationStep> getSourceTransformationSteps(
      NatTranslatedSrcOrDst source) {
    // TODO: Implement visitor to convert translated source to transformation steps.
    //       Will also be dependent on whether transformation is incoming or outgoing.
    return ImmutableList.of();
  }

  public static @Nonnull List<TransformationStep> getDestinationTransformationSteps(
      NatTranslatedSrcOrDst destination) {
    // TODO: Implement visitor to convert translated destination to transformation steps.
    //       Will also be dependent on whether transformation is incoming or outgoing.
    return ImmutableList.of();
  }

  /**
   * Applies a {@link ConcreteService} to its current {@link HeaderSpace.Builder}. Does not modify
   * the headerspace if the given service object is unconstrained.
   */
  private static class NatOriginalServiceConverter implements ServiceVisitor<Void> {
    private @Nullable HeaderSpace.Builder _hsb;

    private NatOriginalServiceConverter() {}

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

  /**
   * Converts a {@link ConcreteSrcOrDst} to an {@link IpSpace}. Returns empty Optional if the given
   * object is inconvertible or unconstrained.
   */
  private static class SrcOrDstToIpSpace implements SrcOrDstVisitor<Optional<IpSpace>> {
    private SrcOrDstToIpSpace() {}

    @Override
    public Optional<IpSpace> visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
      // Does not constrain
      return Optional.empty();
    }

    @Override
    public Optional<IpSpace> visitAddressRange(AddressRange addressRange) {
      return Optional.ofNullable(CheckPointGatewayConversions.toIpSpace(addressRange));
    }

    @Override
    public Optional<IpSpace> visitCpmiGatewayCluster(CpmiGatewayCluster cpmiGatewayCluster) {
      // TODO Implement
      return Optional.empty();
    }

    @Override
    public Optional<IpSpace> visitGroup(Group group) {
      // TODO Implement
      return Optional.empty();
    }

    @Override
    public Optional<IpSpace> visitHost(Host host) {
      // TODO Implement
      return Optional.empty();
    }

    @Override
    public Optional<IpSpace> visitNetwork(Network network) {
      return Optional.of(CheckPointGatewayConversions.toIpSpace(network));
    }
  }

  // TODO Implement
  private static class NatTranslatedServiceConverter
      implements NatTranslatedServiceVisitor<List<TransformationStep>> {
    private NatTranslatedServiceConverter() {}

    @Override
    public List<TransformationStep> visitOriginal(Original original) {
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitPolicyTargets(PolicyTargets policyTargets) {
      return ImmutableList.of();
    }

    @Override
    public List<TransformationStep> visitUnhandledGlobal(UnhandledGlobal unhandledGlobal) {
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
