package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.junit.Test;

public class SourceNatTest {
  @Test
  public void testSerialization() throws IOException {
    SourceNat nat = new SourceNat(null, null, null);
    assertThat(BatfishObjectMapper.clone(nat, SourceNat.class), equalTo(nat));

    IpAccessList acl =
        IpAccessList.builder()
            .setName("foo")
            .setLines(ImmutableList.of(IpAccessListLine.accepting(AclLineMatchExprs.TRUE)))
            .build();
    nat = new SourceNat(acl, new Ip("1.1.1.1"), new Ip("2.2.2.2"));
    assertThat(BatfishObjectMapper.clone(nat, SourceNat.class), equalTo(nat));
  }
}
