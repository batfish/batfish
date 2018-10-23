package org.batfish.main;

import static org.batfish.common.BfConsts.RELPATH_AWS_CONFIGS_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.batfish.common.BfConsts;
import org.batfish.common.Warning;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.junit.Before;
import org.junit.Test;

public class AwsParseWarningsTest {

  private ParseVendorConfigurationAnswerElement _pvcae;
  private Path _path = Paths.get(BfConsts.RELPATH_AWS_CONFIGS_DIR, "region", "file.json");

  @Before
  public void initializeAnswerElement() {
    _pvcae = new ParseVendorConfigurationAnswerElement();
  }

  @Test
  public void testBadJsonWarning() {
    Batfish.parseAwsConfigurations(ImmutableMap.of(_path, "{"), _pvcae);
    assertThat(
        _pvcae.getWarnings().get(RELPATH_AWS_CONFIGS_FILE).getRedFlagWarnings(),
        contains(
            new Warning(String.format("AWS file %s is not valid JSON", _path.toString()), "AWS")));
  }

  @Test
  public void testInvalidKeyWarning() {
    Batfish.parseAwsConfigurations(ImmutableMap.of(_path, "{ \"invalidKey\": [] }"), _pvcae);
    assertThat(
        _pvcae.getWarnings().get(RELPATH_AWS_CONFIGS_FILE).getUnimplementedWarnings(),
        contains(
            new Warning(
                String.format("Unrecognized element 'invalidKey' in AWS file %s", _path.toString()),
                "AWS")));
  }
}
