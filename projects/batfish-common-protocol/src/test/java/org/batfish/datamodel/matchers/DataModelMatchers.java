package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.isis.IsisProcess;
import org.batfish.datamodel.matchers.ConfigurationMatchersImpl.HasRouteFilterList;
import org.batfish.datamodel.matchers.ConfigurationMatchersImpl.HasRouteFilterLists;
import org.batfish.datamodel.matchers.ConfigurationMatchersImpl.HasZone;
import org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.HasNumReferrers;
import org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.HasRedFlagWarning;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasSrcOrDstPorts;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasBandwidth;
import org.batfish.datamodel.matchers.OspfProcessMatchersImpl.HasReferenceBandwidth;
import org.batfish.datamodel.matchers.ParseVendorConfigurationAnswerElementMatchers.HasParseWarning;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasEigrpProcesses;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasIsisProcess;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.vendor.StructureType;
import org.batfish.vendor.StructureUsage;
import org.hamcrest.Matcher;

public final class DataModelMatchers {

  /**
   * Provides a matcher for a collection that matches if each element matched by {@code
   * identifiedBy} is matched by {@code conformsTo}
   */
  public static <E> Matcher<Iterable<? extends E>> forAll(
      Matcher<? super E> identifiedBy, Matcher<? super E> conformsTo) {
    return new ForAll<>(identifiedBy, conformsTo);
  }

  /** Provides a matcher that matches if the RouteFilterList permits the given {@code prefix}. */
  public static RouteFilterListMatchersImpl.Permits permits(@Nonnull Prefix prefix) {
    return new RouteFilterListMatchersImpl.Permits(prefix);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IpSpaceReference}'s {@code name}.
   */
  public static @Nonnull Matcher<PermittedByAcl> hasAclName(
      @Nonnull Matcher<? super String> subMatcher) {
    return new PermittedByAclMatchers.HasAclName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * {@link Zone} with specified name.
   */
  public static Matcher<Configuration> hasZone(
      @Nonnull String name, @Nonnull Matcher<? super Zone> subMatcher) {
    return new HasZone(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code incomingFilter}.
   */
  public static @Nonnull Matcher<Interface> hasIncomingFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new InterfaceMatchersImpl.HasIncomingFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@link Interface#getOutgoingOriginalFlowFilter()} outgoingOriginalFlowFilter}.
   */
  public static @Nonnull Matcher<Interface> hasOutgoingOriginalFlowFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new InterfaceMatchersImpl.HasOutgoingOriginalFlowFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code outgoingFilter}.
   */
  public static @Nonnull Matcher<Interface> hasOutgoingFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new InterfaceMatchersImpl.HasOutgoingFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code outgoingFilterName}.
   */
  public static @Nonnull Matcher<Interface> hasOutgoingFilterName(
      @Nonnull Matcher<? super String> subMatcher) {
    return hasOutgoingFilter(IpAccessListMatchers.hasName(subMatcher));
  }

  /**
   * Provides a matcher that matches if the {@link Interface}'s {@code outgoingFilterName} is equal
   * to {@code expectedName}.
   */
  public static @Nonnull Matcher<Interface> hasOutgoingFilterName(@Nullable String expectedName) {
    return hasOutgoingFilter(IpAccessListMatchers.hasName(expectedName));
  }

  /**
   * Provides a matcher that matches if the provided {@link ParseVendorConfigurationAnswerElement}
   * has a parse warning with comment matched by {@code subMatcher}.
   */
  public static Matcher<ParseVendorConfigurationAnswerElement> hasParseWarning(
      @Nonnull String filename, @Nonnull Matcher<? super String> subMatcher) {
    return new HasParseWarning(filename, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code postTransformationIncomingFilterName}.
   */
  public static @Nonnull Matcher<Interface> hasPostTransformationIncomingFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new InterfaceMatchersImpl.HasPostTransformationIncomingFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code preTransformationOutgoingFilterName}.
   */
  public static @Nonnull Matcher<Interface> hasPreTransformationOutgoingFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new InterfaceMatchersImpl.HasPreTransformationOutgoingFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * {@link RouteFilterList} with specified name.
   */
  public static Matcher<Configuration> hasRouteFilterList(
      @Nonnull String name, @Nonnull Matcher<? super RouteFilterList> subMatcher) {
    return new HasRouteFilterList(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * routeFilterLists.
   */
  public static Matcher<Configuration> hasRouteFilterLists(
      @Nonnull Matcher<? super Map<String, RouteFilterList>> subMatcher) {
    return new HasRouteFilterLists(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has a
   * red-flag warning with text matched by {@code subMatcher}.
   */
  public static Matcher<ConvertConfigurationAnswerElement> hasRedFlagWarning(
      @Nonnull String hostname, @Nonnull Matcher<? super String> subMatcher) {
    return new HasRedFlagWarning(hostname, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code expectedReferenceBandwidth} is equal to
   * the {@link OspfProcess}'s reference-bandwidth.
   */
  public static Matcher<OspfProcess> hasReferenceBandwidth(double expectedReferenceBandwidth) {
    return new HasReferenceBandwidth(equalTo(expectedReferenceBandwidth));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * srcOrDstPorts.
   */
  public static @Nonnull Matcher<HeaderSpace> hasSrcOrDstPorts(
      @Nonnull Matcher<? super SortedSet<SubRange>> subMatcher) {
    return new HasSrcOrDstPorts(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code name} is that of the {@link
   * IpSpaceReference}.
   */
  public static @Nonnull Matcher<PermittedByAcl> hasAclName(@Nonnull String name) {
    return hasAclName(equalTo(name));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * HeaderSpace}'s ipProtcols.
   */
  public static @Nonnull Matcher<HeaderSpace> hasIpProtocols(
      @Nonnull Matcher<? super SortedSet<IpProtocol>> subMatcher) {
    return new HeaderSpaceMatchersImpl.HasIpProtocols(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code bandwidth} is that of the {@link
   * Interface}.
   */
  public static @Nonnull Matcher<Interface> hasBandwidth(double bandwidth) {
    return hasBandwidth(equalTo(bandwidth));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s bandwidth.
   */
  public static @Nonnull Matcher<Interface> hasBandwidth(Matcher<? super Double> subMatcher) {
    return new HasBandwidth(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has a
   * defined structure for {@code filename} of type {@code type} named {@code structureName}.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement> hasDefinedStructure(
      @Nonnull String filename, @Nonnull StructureType type, @Nonnull String structureName) {
    return new ConvertConfigurationAnswerElementMatchers.HasDefinedStructure(
        filename, type, structureName);
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has a
   * defined structure for {@code hostname} of type {@code type} named {@code structureName} with
   * definition lines matching the provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement>
      hasDefinedStructureWithDefinitionLines(
          @Nonnull String hostname,
          @Nonnull StructureType type,
          @Nonnull String structureName,
          @Nonnull Matcher<? super Set<Integer>> subMatcher) {
    return new ConvertConfigurationAnswerElementMatchers.HasDefinedStructureWithDefinitionLines(
        hostname, type, structureName, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link Zone}'s
   * interfaces.
   */
  public static @Nonnull Matcher<Zone> hasMemberInterfaces(
      @Nonnull Matcher<? super SortedSet<String>> subMatcher) {
    return new ZoneMatchers.HasInterfaces(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link Vrf}'s
   * eigrpProcess.
   */
  public static @Nonnull Matcher<Vrf> hasEigrpProcesses(
      @Nonnull Matcher<? super Map<Long, EigrpProcess>> subMatcher) {
    return new HasEigrpProcesses(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link Vrf}'s
   * isisProcess.
   */
  public static @Nonnull Matcher<Vrf> hasIsisProcess(
      @Nonnull Matcher<? super IsisProcess> subMatcher) {
    return new HasIsisProcess(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IpSpaceReference}'s {@code name}.
   */
  public static @Nonnull Matcher<IpSpaceReference> hasName(
      @Nonnull Matcher<? super String> subMatcher) {
    return new IpSpaceReferenceMatchers.HasName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code name} is that of the {@link
   * IpSpaceReference}.
   */
  public static @Nonnull Matcher<IpSpaceReference> hasName(@Nonnull String name) {
    return hasName(equalTo(name));
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has
   * an undefined reference in {@code hostname} to a structure of type {@code type} named {@code
   * structureName}.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement> hasUndefinedReference(
      @Nonnull String hostname, @Nonnull StructureType type, @Nonnull String structureName) {
    return new ConvertConfigurationAnswerElementMatchers.HasUndefinedReference(
        hostname, type, structureName);
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has
   * an undefined reference in {@code filename} to a structure of type {@code type} named {@code
   * structureName} of usage type {@code usage}.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement> hasUndefinedReference(
      @Nonnull String filename,
      @Nonnull StructureType type,
      @Nonnull String structureName,
      @Nonnull StructureUsage usage) {
    return new ConvertConfigurationAnswerElementMatchers.HasUndefinedReferenceWithUsage(
        filename, type, structureName, usage);
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has
   * no undefined references.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement> hasNoUndefinedReferences() {
    return new ConvertConfigurationAnswerElementMatchers.HasNoUndefinedReferences();
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has a
   * reference in {@code filename} to a structure of type {@code type} named {@code structureName}
   * of usage type {@code usage}.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement> hasReferencedStructure(
      @Nonnull String filename,
      @Nonnull StructureType type,
      @Nonnull String structureName,
      @Nonnull StructureUsage usage) {
    return new ConvertConfigurationAnswerElementMatchers.HasReferenceWithUsage(
        filename, type, structureName, usage);
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has
   * an undefined reference in {@code hostname} to a structure of type {@code type} named {@code
   * structureName} of usage type {@code usage} with reference lines matching the provided {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement>
      hasUndefinedReferenceWithReferenceLines(
          @Nonnull String hostname,
          @Nonnull StructureType type,
          @Nonnull String structureName,
          @Nonnull StructureUsage usage,
          @Nonnull Matcher<? super Set<Integer>> subMatcher) {
    return new ConvertConfigurationAnswerElementMatchers.HasUndefinedReferenceWithReferenceLines(
        hostname, type, structureName, usage, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has a
   * structure for {@code filename} of type {@code type} named {@code structureName} with {@code
   * numReferrers} referrers.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement> hasNumReferrers(
      @Nonnull String filename,
      @Nonnull StructureType type,
      @Nonnull String structureName,
      int numReferrers) {
    return new HasNumReferrers(filename, type, structureName, numReferrers);
  }

  /**
   * Provides a matcher that matches if the object is an {@link IpSpaceReference} matched by the
   * provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<IpSpace> isIpSpaceReferenceThat(
      @Nonnull Matcher<? super IpSpaceReference> subMatcher) {
    return new IpSpaceReferenceMatchers.IsIpSpaceReferenceThat(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link PermittedByAcl} matched by the
   * provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<AclLineMatchExpr> isPermittedByAclThat(
      @Nonnull Matcher<? super PermittedByAcl> subMatcher) {
    return new PermittedByAclMatchers.IsPermittedByAclThat(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link
   * org.batfish.datamodel.acl.MatchDestinationIp} matched by the provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<AclLineMatchExpr> isMatchDestinationIpThat(
      @Nonnull Matcher<? super MatchDestinationIp> subMatcher) {
    return new IsInstanceThat<>(MatchDestinationIp.class, subMatcher);
  }

  private DataModelMatchers() {}
}
