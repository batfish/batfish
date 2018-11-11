package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.getRemoteAs;
import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.getRemoteIp;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.junit.Test;

public class BgpPeerPropertySpecifierTest {

  @Test
  public void getRemoteAsActivePeer() {
    BgpActivePeerConfig activePeerConfig = BgpActivePeerConfig.builder().setRemoteAs(100L).build();
    assertThat(getRemoteAs(activePeerConfig), equalTo(new SelfDescribingObject(Schema.LONG, 100L)));
  }

  @Test
  public void getRemoteAsPassivePeer() {
    BgpPassivePeerConfig passivePeerConfig =
        BgpPassivePeerConfig.builder().setRemoteAs(ImmutableList.of(100L)).build();
    assertThat(
        getRemoteAs(passivePeerConfig),
        equalTo(new SelfDescribingObject(Schema.list(Schema.LONG), ImmutableList.of(100L))));
  }

  @Test
  public void getRemoteIpActivePeer() {
    Ip ip = new Ip("1.1.1.1");
    BgpActivePeerConfig activePeerConfig = BgpActivePeerConfig.builder().setPeerAddress(ip).build();
    assertThat(getRemoteIp(activePeerConfig), equalTo(new SelfDescribingObject(Schema.IP, ip)));
  }

  @Test
  public void getRemoteIpPassivePeer() {
    Prefix prefix = new Prefix(new Ip("1.1.1.1"), 23);
    BgpPassivePeerConfig passivePeerConfig =
        BgpPassivePeerConfig.builder().setPeerPrefix(prefix).build();
    assertThat(
        getRemoteIp(passivePeerConfig), equalTo(new SelfDescribingObject(Schema.PREFIX, prefix)));
  }
}
