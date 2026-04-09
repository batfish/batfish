package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.Mlag;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.vendor_family.VendorFamily;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class ConfigurationMatchers {

  public static Matcher<Configuration> hasConfigurationFormat(ConfigurationFormat format) {
    return new HasConfigurationFormat(equalTo(format));
  }

  public static Matcher<Configuration> hasConfigurationFormat(
      @Nonnull Matcher<? super ConfigurationFormat> subMatcher) {
    return new HasConfigurationFormat(subMatcher);
  }

  /** Provides a matcher that matches if the configuration has a default VRF. */
  public static Matcher<Configuration> hasDefaultVrf() {
    return new HasDefaultVrf(not(nullValue()));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * default VRF.
   */
  public static Matcher<Configuration> hasDefaultVrf(@Nonnull Matcher<? super Vrf> subMatcher) {
    return new HasDefaultVrf(subMatcher);
  }

  public static Matcher<Configuration> hasDeviceModel(DeviceModel model) {
    return hasDeviceModel(equalTo(model));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * device model
   */
  public static Matcher<Configuration> hasDeviceModel(
      @Nonnull Matcher<? super DeviceModel> subMatcher) {
    return new HasDeviceModel(subMatcher);
  }

  public static Matcher<Configuration> hasDeviceType(DeviceType type) {
    return hasDeviceType(equalTo(type));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * device type
   */
  public static Matcher<Configuration> hasDeviceType(
      @Nonnull Matcher<? super DeviceType> subMatcher) {
    return new HasDeviceType(subMatcher);
  }

  /**
   * Provides a matcher that matches if the configuration's hostname is {@code expectedHostname}.
   */
  public static @Nonnull Matcher<Configuration> hasHostname(@Nonnull String expectedHostname) {
    return new HasHostname(equalTo(expectedHostname));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * hostname.
   */
  public static @Nonnull Matcher<Configuration> hasHostname(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasHostname(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * IKE phase 1 policy with specified name.
   */
  public static Matcher<Configuration> hasIkePhase1Policy(
      @Nonnull String name, @Nonnull Matcher<? super IkePhase1Policy> subMatcher) {
    return new HasIkePhase1Policy(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * IKE phase 1 proposal with specified name.
   */
  public static Matcher<Configuration> hasIkePhase1Proposal(
      @Nonnull String name, @Nonnull Matcher<? super IkePhase1Proposal> subMatcher) {
    return new HasIkePhase1Proposal(name, subMatcher);
  }

  /** Provides a matcher that matches if the configuration has an interface with the given name. */
  public static Matcher<Configuration> hasInterface(@Nonnull String name) {
    return hasInterface(name, any(Interface.class));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * interface with specified name.
   */
  public static Matcher<Configuration> hasInterface(
      @Nonnull String name, @Nonnull Matcher<? super Interface> subMatcher) {
    return new HasInterface(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * interfaces.
   */
  public static Matcher<Configuration> hasInterfaces(
      @Nonnull Matcher<? super Map<String, Interface>> subMatcher) {
    return new HasInterfaces(subMatcher);
  }

  /**
   * Provides a matcher that matches if the configuration has an IpAccessList with specified name.
   */
  public static Matcher<Configuration> hasIpAccessList(@Nonnull String name) {
    return hasIpAccessList(name, Matchers.any(IpAccessList.class));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * IpAccessList with specified name.
   */
  public static Matcher<Configuration> hasIpAccessList(
      @Nonnull String name, @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasIpAccessList(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * ipAccessLists.
   */
  public static Matcher<Configuration> hasIpAccessLists(
      @Nonnull Matcher<? super Map<String, IpAccessList>> subMatcher) {
    return new HasIpAccessLists(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * IPSec peer config with specified name.
   */
  public static Matcher<Configuration> hasIpsecPeerConfig(
      @Nonnull String name, @Nonnull Matcher<? super IpsecPeerConfig> subMatcher) {
    return new HasIpsecPeerConfig(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * IPSec Phase2 policy with specified name.
   */
  public static Matcher<Configuration> hasIpsecPhase2Policy(
      @Nonnull String name, @Nonnull Matcher<? super IpsecPhase2Policy> subMatcher) {
    return new HasIpsecPhase2Policy(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * IPSec Phase2 proposal with specified name.
   */
  public static Matcher<Configuration> hasIpsecPhase2Proposal(
      @Nonnull String name, @Nonnull Matcher<? super IpsecPhase2Proposal> subMatcher) {
    return new HasIpsecPhase2Proposal(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * IpSpace with specified name.
   */
  public static Matcher<Configuration> hasIpSpace(
      @Nonnull String name, @Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasIpSpace(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * ipSpaces.
   */
  public static Matcher<Configuration> hasIpSpaces(
      @Nonnull Matcher<? super Map<String, IpSpace>> subMatcher) {
    return new HasIpSpaces(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * MLAG configuration with the specified name.
   */
  public static Matcher<Configuration> hasMlagConfig(
      @Nonnull String name, @Nonnull Matcher<? super Mlag> subMatcher) {
    return new HasMlag(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Configuration}'s trackingGroups.
   */
  public static Matcher<Configuration> hasTrackingGroups(
      @Nonnull Matcher<? super Map<String, TrackMethod>> subMatcher) {
    return new HasTrackingGroups(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * vendorFamily.
   */
  public static Matcher<Configuration> hasVendorFamily(
      @Nonnull Matcher<? super VendorFamily> subMatcher) {
    return new HasVendorFamily(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * VRF with specified name.
   */
  public static Matcher<Configuration> hasVrf(
      @Nonnull String name, @Nonnull Matcher<? super Vrf> subMatcher) {
    return new HasVrf(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * vrfs.
   */
  public static Matcher<Configuration> hasVrfs(
      @Nonnull Matcher<? super Map<String, Vrf>> subMatcher) {
    return new HasVrfs(subMatcher);
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
   * Provides a matcher that matches if the provided {@code subMatcher} matches the configuration's
   * {@link Zone} with specified name.
   */
  public static Matcher<Configuration> hasZone(
      @Nonnull String name, @Nonnull Matcher<? super Zone> subMatcher) {
    return new HasZone(name, subMatcher);
  }

  private ConfigurationMatchers() {}

  private static final class HasConfigurationFormat
      extends FeatureMatcher<Configuration, ConfigurationFormat> {
    HasConfigurationFormat(@Nonnull Matcher<? super ConfigurationFormat> subMatcher) {
      super(subMatcher, "a configuration with configurationFormat", "configurationFormat");
    }

    @Override
    protected ConfigurationFormat featureValueOf(Configuration actual) {
      return actual.getConfigurationFormat();
    }
  }

  private static final class HasDefaultVrf extends FeatureMatcher<Configuration, Vrf> {
    HasDefaultVrf(@Nonnull Matcher<? super Vrf> subMatcher) {
      super(subMatcher, "A Configuration with defaultVrf:", "defaultVrf");
    }

    @Override
    protected Vrf featureValueOf(Configuration actual) {
      return actual.getDefaultVrf();
    }
  }

  private static final class HasDeviceModel extends FeatureMatcher<Configuration, DeviceModel> {
    HasDeviceModel(@Nonnull Matcher<? super DeviceModel> subMatcher) {
      super(subMatcher, "a configuration with deviceModel", "deviceModel");
    }

    @Override
    protected DeviceModel featureValueOf(Configuration actual) {
      return actual.getDeviceModel();
    }
  }

  private static final class HasDeviceType extends FeatureMatcher<Configuration, DeviceType> {
    HasDeviceType(@Nonnull Matcher<? super DeviceType> subMatcher) {
      super(subMatcher, "a configuration with deviceType", "deviceType");
    }

    @Override
    protected DeviceType featureValueOf(Configuration actual) {
      return actual.getDeviceType();
    }
  }

  private static final class HasHostname extends FeatureMatcher<Configuration, String> {
    HasHostname(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A Configuration with hostname", "hostname");
    }

    @Override
    protected String featureValueOf(Configuration actual) {
      return actual.getHostname();
    }
  }

  private static final class HasIkePhase1Policy
      extends FeatureMatcher<Configuration, IkePhase1Policy> {
    private final String _name;

    HasIkePhase1Policy(@Nonnull String name, @Nonnull Matcher<? super IkePhase1Policy> subMatcher) {
      super(
          subMatcher,
          "A Configuration with ikePhase1Policy " + name + ":",
          "ikePhase1Policy " + name);
      _name = name;
    }

    @Override
    protected IkePhase1Policy featureValueOf(Configuration actual) {
      return actual.getIkePhase1Policies().get(_name);
    }
  }

  private static final class HasIkePhase1Proposal
      extends FeatureMatcher<Configuration, IkePhase1Proposal> {
    private final String _name;

    HasIkePhase1Proposal(
        @Nonnull String name, @Nonnull Matcher<? super IkePhase1Proposal> subMatcher) {
      super(
          subMatcher,
          "A Configuration with ikePhase1Proposal " + name + ":",
          "ikePhase1Proposal " + name);
      _name = name;
    }

    @Override
    protected IkePhase1Proposal featureValueOf(Configuration actual) {
      return actual.getIkePhase1Proposals().get(_name);
    }
  }

  private static final class HasInterface extends FeatureMatcher<Configuration, Interface> {
    private final String _name;

    HasInterface(@Nonnull String name, @Nonnull Matcher<? super Interface> subMatcher) {
      super(subMatcher, "A Configuration with interface " + name + ":", "interface " + name);
      _name = name;
    }

    @Override
    protected Interface featureValueOf(Configuration actual) {
      return actual.getAllInterfaces().get(_name);
    }
  }

  private static final class HasInterfaces
      extends FeatureMatcher<Configuration, Map<String, Interface>> {
    HasInterfaces(@Nonnull Matcher<? super Map<String, Interface>> subMatcher) {
      super(subMatcher, "a configuration with interfaces", "interfaces");
    }

    @Override
    protected Map<String, Interface> featureValueOf(Configuration actual) {
      return actual.getAllInterfaces();
    }
  }

  private static final class HasIpAccessList extends FeatureMatcher<Configuration, IpAccessList> {
    private final String _name;

    HasIpAccessList(@Nonnull String name, @Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "A Configuration with ipAccessList " + name + ":", "ipAccessList " + name);
      _name = name;
    }

    @Override
    protected IpAccessList featureValueOf(Configuration actual) {
      return actual.getIpAccessLists().get(_name);
    }
  }

  private static final class HasIpAccessLists
      extends FeatureMatcher<Configuration, Map<String, IpAccessList>> {
    HasIpAccessLists(@Nonnull Matcher<? super Map<String, IpAccessList>> subMatcher) {
      super(subMatcher, "a configuration with ipAccessLists", "ipAccessLists");
    }

    @Override
    protected Map<String, IpAccessList> featureValueOf(Configuration actual) {
      return actual.getIpAccessLists();
    }
  }

  private static final class HasIpsecPhase2Policy
      extends FeatureMatcher<Configuration, IpsecPhase2Policy> {
    private final String _name;

    HasIpsecPhase2Policy(
        @Nonnull String name, @Nonnull Matcher<? super IpsecPhase2Policy> subMatcher) {
      super(
          subMatcher,
          "A Configuration with ipsecPhase2Policy " + name + ":",
          "ipsecPhase2Policy " + name);
      _name = name;
    }

    @Override
    protected IpsecPhase2Policy featureValueOf(Configuration actual) {
      return actual.getIpsecPhase2Policies().get(_name);
    }
  }

  private static final class HasIpsecPhase2Proposal
      extends FeatureMatcher<Configuration, IpsecPhase2Proposal> {
    private final String _name;

    HasIpsecPhase2Proposal(
        @Nonnull String name, @Nonnull Matcher<? super IpsecPhase2Proposal> subMatcher) {
      super(
          subMatcher,
          "A Configuration with ipsecPhase2Proposal " + name + ":",
          "ipsecPhase2Proposal " + name);
      _name = name;
    }

    @Override
    protected IpsecPhase2Proposal featureValueOf(Configuration actual) {
      return actual.getIpsecPhase2Proposals().get(_name);
    }
  }

  private static final class HasIpSpace extends FeatureMatcher<Configuration, IpSpace> {
    private final String _name;

    HasIpSpace(@Nonnull String name, @Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "A Configuration with ipSpace " + name + ":", "ipSpace " + name);
      _name = name;
    }

    @Override
    protected IpSpace featureValueOf(Configuration actual) {
      return actual.getIpSpaces().get(_name);
    }
  }

  private static final class HasIpSpaces
      extends FeatureMatcher<Configuration, Map<String, IpSpace>> {
    HasIpSpaces(@Nonnull Matcher<? super Map<String, IpSpace>> subMatcher) {
      super(subMatcher, "a configuration with ipSpaces", "ipSpaces");
    }

    @Override
    protected Map<String, IpSpace> featureValueOf(Configuration actual) {
      return actual.getIpSpaces();
    }
  }

  private static final class HasIpsecPeerConfig
      extends FeatureMatcher<Configuration, IpsecPeerConfig> {
    private final String _name;

    HasIpsecPeerConfig(@Nonnull String name, @Nonnull Matcher<? super IpsecPeerConfig> subMatcher) {
      super(
          subMatcher,
          "A Configuration with ipsecPeerConfig " + name + ":",
          "ipsecPeerConfig " + name);
      _name = name;
    }

    @Override
    protected IpsecPeerConfig featureValueOf(Configuration actual) {
      return actual.getIpsecPeerConfigs().get(_name);
    }
  }

  private static final class HasMlag extends FeatureMatcher<Configuration, Mlag> {
    private final String _name;

    HasMlag(@Nonnull String name, @Nonnull Matcher<? super Mlag> subMatcher) {
      super(subMatcher, "A Configuration with Mlag" + name + ":", "mlag " + name);
      _name = name;
    }

    @Override
    protected Mlag featureValueOf(Configuration actual) {
      return actual.getMlags().get(_name);
    }
  }

  private static final class HasRouteFilterList
      extends FeatureMatcher<Configuration, RouteFilterList> {
    private final String _name;

    HasRouteFilterList(@Nonnull String name, @Nonnull Matcher<? super RouteFilterList> subMatcher) {
      super(
          subMatcher,
          "A Configuration with RouteFilterList " + name + ":",
          "RouteFilterList " + name);
      _name = name;
    }

    @Override
    protected RouteFilterList featureValueOf(Configuration actual) {
      return actual.getRouteFilterLists().get(_name);
    }
  }

  private static final class HasRouteFilterLists
      extends FeatureMatcher<Configuration, Map<String, RouteFilterList>> {
    HasRouteFilterLists(@Nonnull Matcher<? super Map<String, RouteFilterList>> subMatcher) {
      super(subMatcher, "A Configuration with routeFilterLists:", "routeFilterLists");
    }

    @Override
    protected Map<String, RouteFilterList> featureValueOf(Configuration actual) {
      return actual.getRouteFilterLists();
    }
  }

  private static final class HasTrackingGroups
      extends FeatureMatcher<Configuration, Map<String, TrackMethod>> {
    HasTrackingGroups(@Nonnull Matcher<? super Map<String, TrackMethod>> subMatcher) {
      super(subMatcher, "A Configuration with trackingGroups:", "trackingGroups");
    }

    @Override
    protected Map<String, TrackMethod> featureValueOf(Configuration actual) {
      return actual.getTrackingGroups();
    }
  }

  private static final class HasVendorFamily extends FeatureMatcher<Configuration, VendorFamily> {
    HasVendorFamily(@Nonnull Matcher<? super VendorFamily> subMatcher) {
      super(subMatcher, "a configuration with vendorFamily", "vendorFamily");
    }

    @Override
    protected VendorFamily featureValueOf(Configuration actual) {
      return actual.getVendorFamily();
    }
  }

  private static final class HasVrf extends FeatureMatcher<Configuration, Vrf> {
    private final String _name;

    HasVrf(@Nonnull String name, @Nonnull Matcher<? super Vrf> subMatcher) {
      super(subMatcher, "A Configuration with vrf " + name + ":", "vrf " + name);
      _name = name;
    }

    @Override
    protected Vrf featureValueOf(Configuration actual) {
      return actual.getVrfs().get(_name);
    }
  }

  private static final class HasVrfs extends FeatureMatcher<Configuration, Map<String, Vrf>> {
    HasVrfs(@Nonnull Matcher<? super Map<String, Vrf>> subMatcher) {
      super(subMatcher, "a configuration with vrfs", "vrfs");
    }

    @Override
    protected Map<String, Vrf> featureValueOf(Configuration actual) {
      return actual.getVrfs();
    }
  }

  private static final class HasZone extends FeatureMatcher<Configuration, Zone> {
    private final String _name;

    HasZone(@Nonnull String name, @Nonnull Matcher<? super Zone> subMatcher) {
      super(subMatcher, "A Configuration with zone " + name + ":", "zone " + name);
      _name = name;
    }

    @Override
    protected Zone featureValueOf(Configuration actual) {
      return actual.getZones().get(_name);
    }
  }
}
