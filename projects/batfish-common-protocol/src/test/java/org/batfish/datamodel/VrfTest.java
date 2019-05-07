package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ospf.OspfProcess;
import org.junit.Test;

public class VrfTest {

  @Test
  public void testJsonSerialization() throws IOException {
    Vrf v = new Vrf("vrf");
    v.setOspfProcesses(
        ImmutableSortedMap.of(
            "ospf", OspfProcess.builder().setProcessId("ospf").setReferenceBandwidth(1d).build()));
    assertThat(BatfishObjectMapper.clone(v, Vrf.class), equalTo(v));
  }
}
