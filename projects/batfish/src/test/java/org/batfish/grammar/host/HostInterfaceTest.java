package org.batfish.grammar.host;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SourceNat;
import org.batfish.representation.host.HostInterface;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

public class HostInterfaceTest {

  public static class HasPoolIpFirst extends FeatureMatcher<SourceNat, Ip> {

    public HasPoolIpFirst(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "poolIpFirst", "poolIpFirst");
    }

    @Override
    protected Ip featureValueOf(SourceNat actual) {
      return actual.getPoolIpFirst();
    }
  }

  public static class HasPoolIpLast extends FeatureMatcher<SourceNat, Ip> {

    public HasPoolIpLast(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "poolIpLast", "poolIpLast");
    }

    @Override
    protected Ip featureValueOf(SourceNat actual) {
      return actual.getPoolIpLast();
    }
  }

  public static class HasSourceNats extends FeatureMatcher<Interface, List<SourceNat>> {

    public HasSourceNats(Matcher<? super List<SourceNat>> subMatcher) {
      super(subMatcher, "sourceNats", "sourceNats");
    }

    @Override
    protected List<SourceNat> featureValueOf(Interface actual) {
      return actual.getSourceNats();
    }
  }

  public static class IsShared extends FeatureMatcher<HostInterface, Boolean> {

    public IsShared() {
      super(equalTo(true), "shared", "shared");
    }

    @Override
    protected Boolean featureValueOf(HostInterface actual) {
      return actual.getShared();
    }
  }

  public static HasPoolIpFirst hasPoolIpFirst(Matcher<? super Ip> subMatcher) {
    return new HasPoolIpFirst(subMatcher);
  }

  public static HasPoolIpLast hasPoolIpLast(Matcher<? super Ip> subMatcher) {
    return new HasPoolIpLast(subMatcher);
  }

  public static HasSourceNats hasSourceNats(Matcher<? super List<SourceNat>> subMatcher) {
    return new HasSourceNats(subMatcher);
  }

  public static IsShared isShared() {
    return new IsShared();
  }

  private Configuration _c;

  private NetworkFactory _factory;

  private ObjectMapper _mapper;

  private Warnings _w;

  @Before
  public void setup() {
    _mapper = new BatfishObjectMapper();
    _factory = new NetworkFactory();
    _c =
        _factory
            .configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.HOST)
            .setHostname("hostInterfaceTest")
            .build();
    _w = new Warnings();
  }

  @Test
  public void testShared() throws JsonParseException, JsonMappingException, IOException {
    Ip sharedAddress = new Ip("1.0.0.1");
    Prefix sharedPrefix = new Prefix(sharedAddress, 24);
    Prefix nonShared1Prefix = new Prefix("2.0.0.2/24");
    Prefix nonShared2Prefix = new Prefix("3.0.0.2/24");
    String ifaceSharedText =
        "{\"name\":\"shared_interface\", \"prefix\":\""
            + sharedPrefix.toString()
            + "\", \"shared\":true}";
    String ifaceNonShared1Text =
        "{\"name\":\"non_shared1_interface\", \"prefix\":\""
            + nonShared1Prefix.toString()
            + "\", \"shared\":false}";
    String ifaceNonShared2Text =
        "{\"name\":\"non_shared2_interface\", \"prefix\":\"" + nonShared2Prefix.toString() + "\"}";

    HostInterface sharedHostInterface = _mapper.readValue(ifaceSharedText, HostInterface.class);
    HostInterface nonShared1HostInterface =
        _mapper.readValue(ifaceNonShared1Text, HostInterface.class);
    HostInterface nonShared2HostInterface =
        _mapper.readValue(ifaceNonShared2Text, HostInterface.class);
    Interface sharedInterface = sharedHostInterface.toInterface(_c, _w);
    Interface nonShared1Interface = nonShared1HostInterface.toInterface(_c, _w);
    Interface nonShared2Interface = nonShared2HostInterface.toInterface(_c, _w);

    /*
     * Check that shared status from text is propagated into instances with correct defaults.
     */
    assertThat(sharedHostInterface, isShared());
    assertThat(nonShared1HostInterface, not(isShared()));
    assertThat(nonShared2HostInterface, not(isShared()));

    /*
     * The shared interface should contain source NAT info as indicated, while the other interfaces
     * should not contain any source NAT information.
     */
    assertThat(
        sharedInterface,
        hasSourceNats(
            hasItem(
                allOf(
                    hasPoolIpFirst(equalTo(sharedAddress)),
                    hasPoolIpLast(equalTo(sharedAddress))))));
    assertThat(nonShared1Interface, hasSourceNats(empty()));
    assertThat(nonShared2Interface, hasSourceNats(empty()));
  }
}
