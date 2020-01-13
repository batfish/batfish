package org.batfish.datamodel.visitors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Before;
import org.junit.Test;

public final class IpSpaceDescriberTest {

  private static final IpSpaceMetadata TEST_METADATA =
      new IpSpaceMetadata("test_source_name", "test_source_type");

  private static final String TEST_METADATA_DESCRIPTION =
      "'test_source_type' named 'test_source_name'";

  private static final String TEST_NAME = "test_name";

  private IpSpaceDescriber _describerNoNamesNorMetadata;

  @Before
  public void setup() {
    _describerNoNamesNorMetadata = new IpSpaceDescriber(ImmutableMap.of(), ImmutableMap.of());
  }

  @Test
  public void testVisitAclIpSpace() {
    IpSpace lineIpSpace = Ip.parse("1.2.3.4").toIpSpace();
    IpSpace line2IpSpace = Ip.parse("1.2.3.5").toIpSpace();
    String lineIpSpaceName = "lineIpSpace";
    IpSpaceMetadata lineIpSpaceMetadata = new IpSpaceMetadata("line_space_name", "line_space_type");
    IpSpace ipSpace =
        AclIpSpace.builder().thenPermitting(lineIpSpace).thenPermitting(line2IpSpace).build();
    IpSpaceDescriber describerWithMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(ipSpace, TEST_METADATA), ImmutableMap.of(TEST_NAME, ipSpace));
    IpSpaceDescriber describerWithLineMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(lineIpSpace, lineIpSpaceMetadata),
            ImmutableMap.of(lineIpSpaceName, lineIpSpace));

    assertThat(ipSpace.accept(_describerNoNamesNorMetadata), equalTo("[0: 1.2.3.4, 1: 1.2.3.5]"));
    assertThat(
        ipSpace.accept(describerWithLineMetadata),
        equalTo("[0: 'line_space_type' named 'line_space_name', 1: 1.2.3.5]"));
    assertThat(ipSpace.accept(describerWithMetadata), equalTo(TEST_METADATA_DESCRIPTION));
  }

  @Test
  public void testVisitEmptyIpSpace() {
    IpSpace ipSpace = EmptyIpSpace.INSTANCE;
    IpSpaceDescriber describerWithMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(ipSpace, TEST_METADATA), ImmutableMap.of(TEST_NAME, ipSpace));

    assertThat(ipSpace.accept(_describerNoNamesNorMetadata), equalTo(ipSpace.toString()));
    assertThat(ipSpace.accept(describerWithMetadata), equalTo(TEST_METADATA_DESCRIPTION));
  }

  @Test
  public void testVisitIpIpSpace() {
    IpSpace ipSpace = Ip.parse("1.0.0.0").toIpSpace();
    IpSpaceDescriber describerWithMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(ipSpace, TEST_METADATA), ImmutableMap.of(TEST_NAME, ipSpace));

    assertThat(ipSpace.accept(_describerNoNamesNorMetadata), equalTo("1.0.0.0"));
    assertThat(ipSpace.accept(describerWithMetadata), equalTo(TEST_METADATA_DESCRIPTION));
  }

  @Test
  public void testVisitIpSpaceReference() {
    String referencedIpSpaceName = "referenced";
    IpSpace ipSpace = new IpSpaceReference(referencedIpSpaceName);
    IpSpaceDescriber describerWithReferencerMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(ipSpace, TEST_METADATA), ImmutableMap.of(TEST_NAME, ipSpace));
    IpSpaceDescriber describerWithReferencedMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(UniverseIpSpace.INSTANCE, new IpSpaceMetadata("ref_name", "ref_type")),
            ImmutableMap.of(TEST_NAME, ipSpace, referencedIpSpaceName, UniverseIpSpace.INSTANCE));
    IpSpaceDescriber describerWithReferencedButNoMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(),
            ImmutableMap.of(TEST_NAME, ipSpace, referencedIpSpaceName, UniverseIpSpace.INSTANCE));

    assertThat(
        ipSpace.accept(_describerNoNamesNorMetadata), equalTo("An IpSpace named 'referenced'"));
    assertThat(ipSpace.accept(describerWithReferencerMetadata), equalTo(TEST_METADATA_DESCRIPTION));
    assertThat(
        ipSpace.accept(describerWithReferencedMetadata), equalTo("'ref_type' named 'ref_name'"));
    assertThat(
        ipSpace.accept(describerWithReferencedButNoMetadata),
        equalTo("An IpSpace named 'referenced'"));
  }

  @Test
  public void testVisitIpWildcardIpSpace() {
    IpSpace ipSpace = IpWildcard.parse("1.0.1.4:4.3.2.1").toIpSpace();
    IpSpaceDescriber describerWithMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(ipSpace, TEST_METADATA), ImmutableMap.of(TEST_NAME, ipSpace));

    assertThat(ipSpace.accept(_describerNoNamesNorMetadata), equalTo("1.0.1.4:4.3.2.1"));
    assertThat(ipSpace.accept(describerWithMetadata), equalTo(TEST_METADATA_DESCRIPTION));
  }

  @Test
  public void testVisitIpWildcardSetIpSpace() {
    IpSpace ipSpace =
        IpWildcardSetIpSpace.builder()
            .including(IpWildcard.parse("1.0.0.0:1.0.1.0"))
            .excluding(IpWildcard.parse("2.0.0.0:0.1.0.1"))
            .build();
    IpSpaceDescriber describerWithMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(ipSpace, TEST_METADATA), ImmutableMap.of(TEST_NAME, ipSpace));

    assertThat(ipSpace.accept(_describerNoNamesNorMetadata), equalTo(ipSpace.toString()));
    assertThat(ipSpace.accept(describerWithMetadata), equalTo(TEST_METADATA_DESCRIPTION));
  }

  @Test
  public void testVisitPrefixIpSpace() {
    IpSpace ipSpace = Prefix.parse("1.0.0.0/24").toIpSpace();
    IpSpaceDescriber describerWithMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(ipSpace, TEST_METADATA), ImmutableMap.of(TEST_NAME, ipSpace));

    assertThat(ipSpace.accept(_describerNoNamesNorMetadata), equalTo("1.0.0.0/24"));
    assertThat(ipSpace.accept(describerWithMetadata), equalTo(TEST_METADATA_DESCRIPTION));
  }

  @Test
  public void testVisitUniverseIpSpace() {
    IpSpace ipSpace = UniverseIpSpace.INSTANCE;
    IpSpaceDescriber describerWithMetadata =
        new IpSpaceDescriber(
            ImmutableMap.of(ipSpace, TEST_METADATA), ImmutableMap.of(TEST_NAME, ipSpace));

    assertThat(ipSpace.accept(_describerNoNamesNorMetadata), equalTo(ipSpace.toString()));
    assertThat(ipSpace.accept(describerWithMetadata), equalTo(TEST_METADATA_DESCRIPTION));
  }
}
