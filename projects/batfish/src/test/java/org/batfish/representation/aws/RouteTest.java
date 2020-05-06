package org.batfish.representation.aws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.Route.TargetType;
import org.junit.Test;

public class RouteTest {

  @Test
  public void testDeserializationRoutePrefixListId() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/RoutePrefixlistId.json");

    assertThat(
        BatfishObjectMapper.mapper().readValue(text, Route.class),
        equalTo(
            new RoutePrefixListId(
                "pl-63a5400a", State.ACTIVE, "vpce-08ce0df7c620c05dc", TargetType.Gateway)));
  }
}
