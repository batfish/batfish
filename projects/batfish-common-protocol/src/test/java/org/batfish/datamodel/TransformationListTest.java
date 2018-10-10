package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.transformation.StaticNatRule;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.Transformation.RuleAction;
import org.junit.Test;

public class TransformationListTest {

  @Test
  public void testSerializeDeserialize() throws IOException {
    List<Transformation> rules =
        ImmutableList.of(
            StaticNatRule.builder()
                .setAction(RuleAction.SOURCE_INSIDE)
                .setLocalNetwork(Prefix.parse("1.1.1.1/32"))
                .setGlobalNetwork(Prefix.parse("11.11.11.11/32"))
                .build());
    TransformationList list = new TransformationList(rules);

    ObjectMapper mapper = BatfishObjectMapper.mapper();
    String serialization = mapper.writeValueAsString(list);

    TransformationList result = mapper.readValue(serialization, TransformationList.class);

    assertThat(result.getTransformations(), equalTo(list.getTransformations()));
  }
}
