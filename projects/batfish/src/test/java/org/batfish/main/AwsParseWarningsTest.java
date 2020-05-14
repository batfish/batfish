package org.batfish.main;

import static org.batfish.common.BfConsts.RELPATH_AWS_CONFIGS_FILE;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Paths;
import org.batfish.common.BfConsts;
import org.batfish.common.Warning;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.junit.Before;
import org.junit.Test;

public class AwsParseWarningsTest {

  private ParseVendorConfigurationAnswerElement _pvcae;
  private String _key =
      Paths.get(BfConsts.RELPATH_AWS_CONFIGS_DIR, "region", "file.json").toString();

  @Before
  public void initializeAnswerElement() {
    _pvcae = new ParseVendorConfigurationAnswerElement();
  }

  @Test
  public void testBadJsonWarning() {
    Batfish.parseAwsConfigurations(ImmutableMap.of(_key, "{"), _pvcae);
    assertThat(
        _pvcae.getWarnings().get(RELPATH_AWS_CONFIGS_FILE).getRedFlagWarnings(),
        contains(new Warning(String.format("Unexpected content in AWS file %s", _key), "AWS")));
  }

  @Test
  public void testInvalidKeyWarning() {
    Batfish.parseAwsConfigurations(ImmutableMap.of(_key, "{ \"invalidKey\": [1] }"), _pvcae);
    assertThat(
        _pvcae.getWarnings().get(RELPATH_AWS_CONFIGS_FILE).getUnimplementedWarnings(),
        contains(
            new Warning(
                String.format("Unrecognized element 'invalidKey' in AWS file %s", _key), "AWS")));
  }
}
