package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.IsDynamic;
import org.batfish.datamodel.matchers.ConfigurationMatchersImpl.HasRoute6FilterList;
import org.batfish.datamodel.matchers.ConfigurationMatchersImpl.HasRouteFilterList;
import org.batfish.datamodel.matchers.ConfigurationMatchersImpl.HasZone;
import org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.HasNumReferrers;
import org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.HasRedFlagWarning;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasSrcOrDstPorts;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasBandwidth;
import org.batfish.datamodel.matchers.OspfProcessMatchersImpl.HasReferenceBandwidth;
import org.batfish.vendor.StructureType;
import org.batfish.vendor.StructureUsage;
import org.hamcrest.Matcher;

public final class DataModelMatchers {

  /** Provides a matcher that matches if the RouteFilterList permits the given {@code prefix}. */
  public static RouteFilterListMatchersImpl.Permits permits(@Nonnull Prefix prefix) {
    return new RouteFilterListMatchersImpl.Permits(prefix);
  }

  /** Provides a matcher that matches if the Route6FilterList permits the given {@code prefix}. */
  public static Route6FilterListMatchersImpl.Permits permits(@Nonnull Prefix6 prefix) {
    return new Route6FilterListMatchersImpl.Permits(prefix);
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
   * Interface}'s {@code outgoingFilterName}.
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
    return new InterfaceMatchersImpl.HasOutgoingFilterName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link Interface}'s {@code outgoingFilterName} is equal
   * to {@code expectedName}.
   */
  public static @Nonnull Matcher<Interface> hasOutgoingFilterName(@Nullable String expectedName) {
    return new InterfaceMatchersImpl.HasOutgoingFilterName(equalTo(expectedName));
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
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has a
   * red-flag warning with text matched by {@code subMatcher}.
   */
  public static Matcher<ConvertConfigurationAnswerElement> hasRedFlagWarning(
      @Nonnull String hostname, @Nonnull Matcher<? super String> subMatcher) {
    return new HasRedFlagWarning(hostname, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * OspfProcess}'s reference-bandwidth.
   */
  public static Matcher<OspfProcess> hasReferenceBandwidth(
      @Nonnull Matcher<? super Double> subMatcher) {
    return new HasReferenceBandwidth(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * {@link Route6FilterList} with specified name.
   */
  public static Matcher<Configuration> hasRoute6FilterList(
      @Nonnull String name, @Nonnull Matcher<? super Route6FilterList> subMatcher) {
    return new HasRoute6FilterList(name, subMatcher);
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
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link Zone}'s
   * interfaces.
   */
  public static @Nonnull Matcher<Zone> hasMemberInterfaces(
      @Nonnull Matcher<? super SortedSet<String>> subMatcher) {
    return new ZoneMatchers.HasInterfaces(subMatcher);
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
   * an undefined refrence in {@code hostname} to a structure of type {@code type} named {@code
   * structureName}.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement> hasUndefinedReference(
      @Nonnull String hostname, @Nonnull StructureType type, @Nonnull String structureName) {
    return new ConvertConfigurationAnswerElementMatchers.HasUndefinedReference(
        hostname, type, structureName);
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has
   * an undefined refrence in {@code hostname} to a structure of type {@code type} named {@code
   * structureName} of usage type {@code usage}.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement> hasUndefinedReference(
      @Nonnull String hostname,
      @Nonnull StructureType type,
      @Nonnull String structureName,
      @Nonnull StructureUsage usage) {
    return new ConvertConfigurationAnswerElementMatchers.HasUndefinedReferenceWithUsage(
        hostname, type, structureName, usage);
  }

  /**
   * Provides a matcher that matches if the provided {@link ConvertConfigurationAnswerElement} has
   * an unused structure for {@code hostname} of type {@code type} named {@code structureName}.
   */
  public static @Nonnull Matcher<ConvertConfigurationAnswerElement> hasNumReferrers(
      @Nonnull String hostname,
      @Nonnull StructureType type,
      @Nonnull String structureName,
      int numReferrers) {
    return new HasNumReferrers(hostname, type, structureName, numReferrers);
  }

  /**
   * Provides a matcher that matches if the {@link BgpNeighbor} is configured as a listening end of
   * a dynamic BGP peering.
   */
  public static @Nonnull Matcher<BgpNeighbor> isDynamic() {
    return new IsDynamic(equalTo(true));
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

  private DataModelMatchers() {}
}
