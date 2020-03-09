package org.batfish.question.ospfinterface;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.junit.Test;

/** Tests for {@link OspfInterfaceConfigurationQuestion} */
public class OspfInterfaceConfigurationQuestionTest {

  @Test
  public void testJsonSerialization() {
    OspfInterfaceConfigurationQuestion q =
        new OspfInterfaceConfigurationQuestion("nodes", InterfacePropertySpecifier.ACCESS_VLAN);
    assertThat(BatfishObjectMapper.clone(q, OspfInterfaceConfigurationQuestion.class), equalTo(q));
  }
}
