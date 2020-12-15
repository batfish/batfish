package org.batfish.representation.aws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link Address} */
public class AddressTest {

  @Test
  public void testConstructor() throws IOException {
    String text =
        "{\"AllocationId\": \"eipalloc-c0da79fd\", \"Domain\": \"vpc\", \"PublicIp\":"
            + " \"34.214.188.89\"}";
    assertThat(
        BatfishObjectMapper.mapper().readValue(text, Address.class),
        equalTo(new Address(Ip.parse("34.214.188.89"), null, null, "eipalloc-c0da79fd")));
  }
}
