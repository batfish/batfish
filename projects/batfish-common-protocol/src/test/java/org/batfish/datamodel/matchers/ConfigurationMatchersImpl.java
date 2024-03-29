package org.batfish.datamodel.matchers;

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

final class ConfigurationMatchersImpl {

  static final class HasConfigurationFormat
      extends FeatureMatcher<Configuration, ConfigurationFormat> {
    HasConfigurationFormat(@Nonnull Matcher<? super ConfigurationFormat> subMatcher) {
      super(subMatcher, "a configuration with configurationFormat", "configurationFormat");
    }

    @Override
    protected ConfigurationFormat featureValueOf(Configuration actual) {
      return actual.getConfigurationFormat();
    }
  }

  static final class HasDefaultVrf extends FeatureMatcher<Configuration, Vrf> {
    HasDefaultVrf(@Nonnull Matcher<? super Vrf> subMatcher) {
      super(subMatcher, "A Configuration with defaultVrf:", "defaultVrf");
    }

    @Override
    protected Vrf featureValueOf(Configuration actual) {
      return actual.getDefaultVrf();
    }
  }

  static final class HasDeviceModel extends FeatureMatcher<Configuration, DeviceModel> {
    HasDeviceModel(@Nonnull Matcher<? super DeviceModel> subMatcher) {
      super(subMatcher, "a configuration with deviceModel", "deviceModel");
    }

    @Override
    protected DeviceModel featureValueOf(Configuration actual) {
      return actual.getDeviceModel();
    }
  }

  static final class HasDeviceType extends FeatureMatcher<Configuration, DeviceType> {
    HasDeviceType(@Nonnull Matcher<? super DeviceType> subMatcher) {
      super(subMatcher, "a configuration with deviceType", "deviceType");
    }

    @Override
    protected DeviceType featureValueOf(Configuration actual) {
      return actual.getDeviceType();
    }
  }

  static final class HasHostname extends FeatureMatcher<Configuration, String> {
    HasHostname(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A Configuration with hostname", "hostname");
    }

    @Override
    protected String featureValueOf(Configuration actual) {
      return actual.getHostname();
    }
  }

  static final class HasIkePhase1Policy extends FeatureMatcher<Configuration, IkePhase1Policy> {
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

  static final class HasIkePhase1Proposal extends FeatureMatcher<Configuration, IkePhase1Proposal> {
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

  static final class HasInterface extends FeatureMatcher<Configuration, Interface> {
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

  static final class HasInterfaces extends FeatureMatcher<Configuration, Map<String, Interface>> {
    HasInterfaces(@Nonnull Matcher<? super Map<String, Interface>> subMatcher) {
      super(subMatcher, "a configuration with interfaces", "interfaces");
    }

    @Override
    protected Map<String, Interface> featureValueOf(Configuration actual) {
      return actual.getAllInterfaces();
    }
  }

  static final class HasIpAccessList extends FeatureMatcher<Configuration, IpAccessList> {
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

  static final class HasIpAccessLists
      extends FeatureMatcher<Configuration, Map<String, IpAccessList>> {
    HasIpAccessLists(@Nonnull Matcher<? super Map<String, IpAccessList>> subMatcher) {
      super(subMatcher, "a configuration with ipAccessLists", "ipAccessLists");
    }

    @Override
    protected Map<String, IpAccessList> featureValueOf(Configuration actual) {
      return actual.getIpAccessLists();
    }
  }

  static final class HasIpsecPhase2Policy extends FeatureMatcher<Configuration, IpsecPhase2Policy> {
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

  static final class HasIpsecPhase2Proposal
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

  static final class HasIpSpace extends FeatureMatcher<Configuration, IpSpace> {
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

  static final class HasIpSpaces extends FeatureMatcher<Configuration, Map<String, IpSpace>> {
    HasIpSpaces(@Nonnull Matcher<? super Map<String, IpSpace>> subMatcher) {
      super(subMatcher, "a configuration with ipSpaces", "ipSpaces");
    }

    @Override
    protected Map<String, IpSpace> featureValueOf(Configuration actual) {
      return actual.getIpSpaces();
    }
  }

  static final class HasIpsecPeerConfig extends FeatureMatcher<Configuration, IpsecPeerConfig> {
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

  static final class HasMlag extends FeatureMatcher<Configuration, Mlag> {
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

  static final class HasRouteFilterList extends FeatureMatcher<Configuration, RouteFilterList> {
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

  static final class HasRouteFilterLists
      extends FeatureMatcher<Configuration, Map<String, RouteFilterList>> {
    HasRouteFilterLists(@Nonnull Matcher<? super Map<String, RouteFilterList>> subMatcher) {
      super(subMatcher, "A Configuration with routeFilterLists:", "routeFilterLists");
    }

    @Override
    protected Map<String, RouteFilterList> featureValueOf(Configuration actual) {
      return actual.getRouteFilterLists();
    }
  }

  static final class HasTrackingGroups
      extends FeatureMatcher<Configuration, Map<String, TrackMethod>> {
    HasTrackingGroups(@Nonnull Matcher<? super Map<String, TrackMethod>> subMatcher) {
      super(subMatcher, "A Configuration with trackingGroups:", "trackingGroups");
    }

    @Override
    protected Map<String, TrackMethod> featureValueOf(Configuration actual) {
      return actual.getTrackingGroups();
    }
  }

  static final class HasVendorFamily extends FeatureMatcher<Configuration, VendorFamily> {
    HasVendorFamily(@Nonnull Matcher<? super VendorFamily> subMatcher) {
      super(subMatcher, "a configuration with vendorFamily", "vendorFamily");
    }

    @Override
    protected VendorFamily featureValueOf(Configuration actual) {
      return actual.getVendorFamily();
    }
  }

  static final class HasVrf extends FeatureMatcher<Configuration, Vrf> {
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

  static final class HasVrfs extends FeatureMatcher<Configuration, Map<String, Vrf>> {
    HasVrfs(@Nonnull Matcher<? super Map<String, Vrf>> subMatcher) {
      super(subMatcher, "a configuration with vrfs", "vrfs");
    }

    @Override
    protected Map<String, Vrf> featureValueOf(Configuration actual) {
      return actual.getVrfs();
    }
  }

  static final class HasZone extends FeatureMatcher<Configuration, Zone> {
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

  private ConfigurationMatchersImpl() {}
}
