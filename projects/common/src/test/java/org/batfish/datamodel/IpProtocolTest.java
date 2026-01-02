package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class IpProtocolTest {

  @Test
  public void testFromString() {
    for (IpProtocol ipProtocol : IpProtocol.values()) {
      assertThat(IpProtocol.fromString(ipProtocol.name()), equalTo(ipProtocol));
    }
  }
}
