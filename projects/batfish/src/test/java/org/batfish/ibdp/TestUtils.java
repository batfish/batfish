package org.batfish.ibdp;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;

public class TestUtils {
  public static Node makeIosRouter(String hostname) {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname(hostname)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    return new Node(c);
  }
}
