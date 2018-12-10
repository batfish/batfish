package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.junit.Test;

public class DestinationNatTest {
  @Test
  public void testSerialization() throws IOException {
    DestinationNat dnat = new DestinationNat(null, null, null);
    assertThat(BatfishObjectMapper.clone(dnat, DestinationNat.class), equalTo(dnat));

    IpAccessList acl =
        IpAccessList.builder()
            .setName("foo")
            .setLines(ImmutableList.of(IpAccessListLine.accepting(AclLineMatchExprs.TRUE)))
            .build();
    dnat = new DestinationNat(acl, new Ip("1.1.1.1"), new Ip("2.2.2.2"));
    assertThat(BatfishObjectMapper.clone(dnat, DestinationNat.class), equalTo(dnat));
  }
}
