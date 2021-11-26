package org.batfish.vendor.sonic.representation;

import static org.batfish.common.BfConsts.RELPATH_SONIC_CONFIGDB_DIR;
import static org.batfish.vendor.sonic.representation.SonicConfigDbs.fromJson;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.vendor.sonic.representation.ConfigDb.Data;
import org.batfish.vendor.sonic.representation.ConfigDbObject.Type;
import org.junit.Test;

public class SonicConfigDbsTest extends TestCase {

  @Test
  public void testJavaSerialization() {
    SonicConfigDbs obj = new SonicConfigDbs(ImmutableMap.of());
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new SonicConfigDbs(ImmutableMap.of()), new SonicConfigDbs(ImmutableMap.of()))
        .addEqualityGroup(
            new SonicConfigDbs(
                ImmutableMap.of("h1", new ConfigDb("file", new Data(ImmutableMap.of())))))
        .testEquals();
  }

  @Test
  public void testFromJson() {
    String file1 = "{ \"DEVICE_METADATA\": { \"localhost\": { \"hostname\": \"name1\" }}}";
    String file2 = "{ \"DEVICE_METADATA\": { \"localhost\": { \"hostname\": \"name2\" }}}";

    // duplicate hostnames
    String file3 = "{ \"DEVICE_METADATA\": { \"localhost\": { \"hostname\": \"name3_4\" }}}";
    String file4 = "{ \"DEVICE_METADATA\": { \"localhost\": { \"hostname\": \"name3_4\" }}}";

    // missing hostname
    String file5 = "{ \"DEVICE_METADATA\": { \"localhost\": { }}}";

    ParseVendorConfigurationAnswerElement element = new ParseVendorConfigurationAnswerElement();

    // the two files with non-duplicate names are included
    assertThat(
        fromJson(
            ImmutableMap.of(
                "file1", file1, "file2", file2, "file3", file3, "file4", file4, "file5", file5),
            element),
        equalTo(
            new SonicConfigDbs(
                ImmutableMap.of(
                    "name1",
                    new ConfigDb(
                        "file1",
                        new Data(
                            ImmutableMap.of(
                                Type.DEVICE_METADATA,
                                new DeviceMetadata(ImmutableMap.of("hostname", "name1"))))),
                    "name2",
                    new ConfigDb(
                        "file2",
                        new Data(
                            ImmutableMap.of(
                                Type.DEVICE_METADATA,
                                new DeviceMetadata(ImmutableMap.of("hostname", "name2")))))))));

    // warnings are logged
    assertThat(
        Iterables.getOnlyElement(
                element.getWarnings().get(RELPATH_SONIC_CONFIGDB_DIR).getRedFlagWarnings())
            .getText(),
        equalTo("Duplicate hostname name3_4 in configdb files [file3, file4]"));
    assertThat(
        Iterables.getOnlyElement(element.getWarnings().get("file5").getRedFlagWarnings()).getText(),
        equalTo("Missing hostname"));
  }
}
