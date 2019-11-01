package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.datamodel.Flow.Builder;
import org.junit.Test;

/** Tests for {@link HeaderSpaceToFlow} */
public class HeaderSpaceToFlowTest {

  @Test
  public void testGetRepresentativeFlow_withoutIpSpaces() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(ImmutableMap.of(), FlowPreference.APPLICATION);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder().setSrcIps(Ip.parse("1.2.3.4").toIpSpace()).build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getSrcIp(), equalTo(Ip.parse("1.2.3.4")));
  }

  @Test
  public void testGetRepresentativeFlow_withIpSpaces() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(
            ImmutableMap.of("ipSpace", Ip.parse("1.2.3.4").toIpSpace()),
            FlowPreference.APPLICATION);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder().setSrcIps(new IpSpaceReference("ipSpace")).build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getSrcIp(), equalTo(Ip.parse("1.2.3.4")));
  }
}
