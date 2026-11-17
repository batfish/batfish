package org.batfish.question.ospfinterface;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.junit.jupiter.api.Test;

/** Tests for {@link OspfInterfaceConfigurationQuestion} */
class OspfInterfaceConfigurationQuestionTest {

  @Test
  void testJsonSerialization() {
    OspfInterfaceConfigurationQuestion q =
        new OspfInterfaceConfigurationQuestion("nodes", InterfacePropertySpecifier.ACCESS_VLAN);
    assertThat(BatfishObjectMapper.clone(q, OspfInterfaceConfigurationQuestion.class), equalTo(q));
  }
}
