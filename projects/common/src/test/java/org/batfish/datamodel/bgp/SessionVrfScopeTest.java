package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.SessionVrfScope.AnyVrf;
import org.batfish.datamodel.bgp.SessionVrfScope.OwnVrf;
import org.batfish.datamodel.bgp.SessionVrfScope.SpecificVrf;
import org.junit.Test;

/** Tests of {@link SessionVrfScope}. */
public class SessionVrfScopeTest {

  private static final ImmutableSet<String> NODE_VRFS = ImmutableSet.of("default", "blue", "green");

  @Test
  public void testOriginVrf() {
    assertThat(OwnVrf.instance().originVrf("blue"), equalTo("blue"));
    assertThat(new SpecificVrf("default").originVrf("blue"), equalTo("default"));
    // AnyVrf collapses to the config VRF for origination.
    assertThat(AnyVrf.instance().originVrf("blue"), equalTo("blue"));
  }

  @Test
  public void testListenVrfs() {
    assertThat(OwnVrf.instance().listenVrfs("blue", NODE_VRFS), contains("blue"));
    assertThat(new SpecificVrf("default").listenVrfs("blue", NODE_VRFS), contains("default"));
    // AnyVrf registers under every VRF on the node.
    assertThat(
        AnyVrf.instance().listenVrfs("blue", NODE_VRFS),
        containsInAnyOrder("default", "blue", "green"));
  }

  @Test
  public void testAcceptsIngressVrf() {
    assertTrue(OwnVrf.instance().acceptsIngressVrf("blue", "blue"));
    assertFalse(OwnVrf.instance().acceptsIngressVrf("green", "blue"));

    assertTrue(new SpecificVrf("default").acceptsIngressVrf("default", "blue"));
    assertFalse(new SpecificVrf("default").acceptsIngressVrf("blue", "blue"));

    // AnyVrf accepts from any ingress VRF.
    assertTrue(AnyVrf.instance().acceptsIngressVrf("default", "blue"));
    assertTrue(AnyVrf.instance().acceptsIngressVrf("green", "blue"));
  }

  @Test
  public void testDisplayString() {
    assertThat(OwnVrf.instance().displayString(), nullValue());
    assertThat(new SpecificVrf("default").displayString(), equalTo("default"));
    assertThat(AnyVrf.instance().displayString(), equalTo("*"));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(OwnVrf.instance(), new OwnVrf())
        .addEqualityGroup(AnyVrf.instance(), new AnyVrf())
        .addEqualityGroup(new SpecificVrf("blue"), new SpecificVrf("blue"))
        .addEqualityGroup(new SpecificVrf("green"))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    for (SessionVrfScope scope :
        new SessionVrfScope[] {OwnVrf.instance(), new SpecificVrf("blue"), AnyVrf.instance()}) {
      assertThat(BatfishObjectMapper.clone(scope, SessionVrfScope.class), equalTo(scope));
    }
  }

  @Test
  public void testJsonDeserializeAnyVrf() throws IOException {
    assertThat(
        BatfishObjectMapper.mapper().readValue("{\"type\":\"AnyVrf\"}", SessionVrfScope.class),
        instanceOf(AnyVrf.class));
  }

  @Test
  public void testJsonDeserializeSpecificVrf() throws IOException {
    assertThat(
        BatfishObjectMapper.mapper()
            .readValue("{\"type\":\"SpecificVrf\",\"vrf\":\"blue\"}", SessionVrfScope.class),
        equalTo(new SpecificVrf("blue")));
  }
}
