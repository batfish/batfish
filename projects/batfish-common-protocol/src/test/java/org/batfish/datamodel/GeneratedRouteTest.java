package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.GeneratedRoute.Builder;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link GeneratedRoute} */
@RunWith(JUnit4.class)
public class GeneratedRouteTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEquals() {
    GeneratedRoute.Builder gr =
        GeneratedRoute.builder().setNetwork(Prefix.parse("1.1.1.0/24")).setMetric(1L);
    new EqualsTester()
        .addEqualityGroup(gr.build(), gr.build())
        .addEqualityGroup(gr.setMetric(2L).build())
        .addEqualityGroup(gr.setDiscard(true).build())
        .addEqualityGroup(gr.setCommunities(CommunitySet.of(StandardCommunity.of(111L))).build())
        .addEqualityGroup(gr.setGenerationPolicy("GENERATE").build())
        .addEqualityGroup(gr.setAttributePolicy("Attribute").build())
        .addEqualityGroup(gr.setAdmin(100).build())
        .addEqualityGroup(gr.setTag(5L).build())
        .addEqualityGroup(gr.setNonRouting(true).build())
        .addEqualityGroup(gr.setNonForwarding(true).build())
        .addEqualityGroup(gr.setLocalPreference(1L).build())
        .addEqualityGroup(gr.setOriginType(OriginType.IGP).build())
        .addEqualityGroup(gr.setWeight(1).build())
        .testEquals();
  }

  @Test
  public void testComparator() {
    Builder grb =
        GeneratedRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setGenerationPolicy("a")
            .setAttributePolicy("b");
    List<GeneratedRoute> ordered =
        ImmutableList.<GeneratedRoute>builder()
            .add(grb.build())
            .add(grb.setAsPath(AsPath.ofSingletonAsSets(ImmutableList.of(1L, 1L))).build())
            .add(grb.setGenerationPolicy(null).build())
            .add(grb.setCommunities(CommunitySet.of(StandardCommunity.of(111L))).build())
            .add(grb.setDiscard(true).build())
            .add(grb.setAttributePolicy(null).build())
            .add(grb.setTag(5L).build())
            .add(grb.setLocalPreference(101L).build())
            .add(grb.setOriginType(OriginType.IGP).build())
            .add(grb.setWeight(1).build())
            .build();
    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(ordered.get(i).compareTo(ordered.get(j)), equalTo(Integer.signum(i - j)));
      }
    }
  }

  @Test
  public void testJavaSerialization() {
    GeneratedRoute gr =
        GeneratedRoute.builder().setNetwork(Prefix.parse("1.1.1.0/24")).setMetric(1L).build();
    assertThat(SerializationUtils.clone(gr), equalTo(gr));
  }

  @Test
  public void testJsonSerialization() {
    GeneratedRoute gr =
        GeneratedRoute.builder().setNetwork(Prefix.parse("1.1.1.0/24")).setMetric(1L).build();
    assertThat(BatfishObjectMapper.clone(gr, GeneratedRoute.class), equalTo(gr));
  }

  @Test
  public void testToBuilder() {
    GeneratedRoute gr =
        GeneratedRoute.builder().setNetwork(Prefix.parse("1.1.1.0/24")).setMetric(1L).build();
    assertThat(gr.toBuilder().build(), equalTo(gr));
  }

  @Test
  public void testThrowsNoNetwork() {
    thrown.expect(IllegalArgumentException.class);
    GeneratedRoute.builder().setMetric(1).build();
  }
}
