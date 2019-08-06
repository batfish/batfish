package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.batfish.common.BfConsts.RELPATH_AWS_CONFIGS_DIR;
import static org.batfish.representation.aws.InternetGateway.AWS_BACKBONE_AS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedMap;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.IspModelingUtils;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** E2e tests that connectivity to and from the internet works as expected */
public class AwsConfigurationInternetConnectivityTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-internet-connectivity/";

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = loadAwsConfigurations(TESTCONFIGS_DIR);
    _batfish.computeDataPlane();
  }

  private static Batfish loadAwsConfigurations(String resourceFolder) throws IOException {
    Path path =
        Paths.get(
            Thread.currentThread().getContextClassLoader().getResource(resourceFolder).getPath());
    String pathPrefixToRemove =
        path.resolve(RELPATH_AWS_CONFIGS_DIR).toAbsolutePath().toString() + File.separator;
    List<String> fileNames =
        Files.walk(path)
            .filter(f -> Files.isRegularFile(f))
            .map(f -> f.toAbsolutePath().toString().split(pathPrefixToRemove)[1])
            .collect(ImmutableList.toImmutableList());
    return BatfishTestUtils.getBatfishFromTestrigText(
        TestrigText.builder().setAwsText(resourceFolder, fileNames).build(), _folder);
  }

  private static void testTrace(
      String ingressNode,
      Ip dstIp,
      FlowDisposition expectedDisposition,
      List<String> expectedNodes) {
    Flow flow =
        Flow.builder()
            .setTag("test")
            .setIngressNode(ingressNode)
            .setDstIp(dstIp) // this public IP does not exists in the network
            .build();
    SortedMap<Flow, List<Trace>> traces =
        _batfish.getTracerouteEngine().computeTraces(ImmutableSet.of(flow), false);

    Trace trace = getOnlyElement(traces.get(flow).iterator());

    assertThat(
        trace.getHops().stream()
            .map(h -> h.getNode().getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(expectedNodes));
    assertThat(trace.getDisposition(), equalTo(expectedDisposition));
  }

  @Test
  public void testFromInternetToPublicIp() {
    // to a valid public IP
    testTrace(
        IspModelingUtils.INTERNET_HOST_NAME,
        Ip.parse("54.191.107.22"),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(
            IspModelingUtils.INTERNET_HOST_NAME,
            IspModelingUtils.getIspNodeName(AWS_BACKBONE_AS),
            "igw-fac5839d",
            "vpc-b390fad5",
            "subnet-073b8061",
            "i-075dc46a9bc347264"));

    // to an invalid public IP
    testTrace(
        IspModelingUtils.INTERNET_HOST_NAME,
        Ip.parse("54.191.107.23"),
        FlowDisposition.EXITS_NETWORK,
        ImmutableList.of(IspModelingUtils.INTERNET_HOST_NAME));
  }

  @Test
  public void testFromInternetToPrivateIp() {
    // we get insufficient info for private IPs that exist somewhere in the network
    testTrace(
        IspModelingUtils.INTERNET_HOST_NAME,
        Ip.parse("192.168.2.18"),
        FlowDisposition.INSUFFICIENT_INFO,
        ImmutableList.of(IspModelingUtils.INTERNET_HOST_NAME));

    // we get exits network for arbitrary private IPs
    testTrace(
        IspModelingUtils.INTERNET_HOST_NAME,
        Ip.parse("192.18.0.8"),
        FlowDisposition.EXITS_NETWORK,
        ImmutableList.of(IspModelingUtils.INTERNET_HOST_NAME));
  }

  @Test
  public void testToInternet() {
    testTrace(
        "i-075dc46a9bc347264",
        Ip.parse("8.8.8.8"),
        FlowDisposition.EXITS_NETWORK,
        ImmutableList.of(
            "i-075dc46a9bc347264",
            "subnet-073b8061",
            "vpc-b390fad5",
            "igw-fac5839d",
            IspModelingUtils.getIspNodeName(AWS_BACKBONE_AS),
            IspModelingUtils.INTERNET_HOST_NAME));
  }
}
