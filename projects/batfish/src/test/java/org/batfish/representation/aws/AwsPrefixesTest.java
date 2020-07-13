package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsPrefixes.SERVICE_AMAZON;
import static org.batfish.representation.aws.AwsPrefixes.SERVICE_EC2;
import static org.batfish.representation.aws.AwsPrefixes.getAwsServicesPrefixes;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.AwsPrefixes.AwsPrefix;
import org.junit.Test;

public class AwsPrefixesTest {

  @Test
  public void testGetPrefixes() {
    assertTrue(AwsPrefixes.getPrefixes().size() > 0);
  }

  @Test
  public void testGetPrefixes_service() {
    assertTrue(AwsPrefixes.getPrefixes(SERVICE_AMAZON).size() > 0);
    assertTrue(AwsPrefixes.getPrefixes(SERVICE_AMAZON).size() < AwsPrefixes.getPrefixes().size());
  }

  @Test
  public void testGetAwsServicesPrefix() {
    Prefix onlyAmazon = Prefix.parse("1.0.0.0/24");
    Prefix onlyEc2 = Prefix.parse("2.0.0.0/24");
    Prefix conflict = Prefix.parse("3.0.0.0/24");
    Prefix onlyOther = Prefix.parse("4.0.0.0/24");

    List<AwsPrefix> all =
        ImmutableList.of(
            new AwsPrefix(onlyAmazon, SERVICE_AMAZON),
            new AwsPrefix(onlyEc2, SERVICE_AMAZON), // all prefixes are amazon prefixes too
            new AwsPrefix(conflict, SERVICE_AMAZON),
            new AwsPrefix(onlyOther, SERVICE_AMAZON),
            new AwsPrefix(onlyEc2, SERVICE_EC2),
            new AwsPrefix(conflict, SERVICE_EC2),
            new AwsPrefix(conflict, "conflictingService"),
            new AwsPrefix(onlyOther, "otherService"));

    assertThat(
        getAwsServicesPrefixes(all), equalTo(ImmutableSet.of(onlyAmazon, conflict, onlyOther)));
  }

  @Test
  public void testGetAwsS3Prefixes() {
    List<Prefix> s3Prefixes = AwsPrefixes.getAwsS3Prefixes();
    assertFalse(s3Prefixes.isEmpty());
  }
}
