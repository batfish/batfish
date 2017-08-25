package org.batfish.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.pojo.Analysis;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link FileStorageImpl} */
public class FileStorageImplTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Storage _storage;

  @Before
  public void setup() throws IOException {
    _storage = new FileStorageImpl(_folder.newFolder("containers").toPath());
  }

  @Test
  public void testInvalidGetAnalysis() {
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' doesn't exist for container '%s'", "fakeanalysis", "fakecontainer"));

    //throws since the analysis is not found in the container
    _storage.getAnalysis("fakecontainer", "fakeanalysis");
  }

  @Test
  public void testSaveAnalysis() {
    Map<String, String> question =
        Collections.singletonMap("testquestionname", "testquestionvalue");
    Analysis analysis = new Analysis("testanalysis", question);
    Analysis persistedCopy = _storage.saveAnalysis("testcontainer", analysis);

    //Test equality of persistent and transient instances
    Assert.assertThat(persistedCopy, equalTo(analysis));
  }

  @Test
  public void testInvalidSaveAnalysis() {
    String containerName = "testcontainer";
    String analysisNmae = "testanalysis";
    Map<String, String> question =
        Collections.singletonMap("testquestionname", "testquestionvalue");
    Analysis analysis = new Analysis(analysisNmae, question);
    _storage.saveAnalysis(containerName, analysis);

    //Test that save analysis throws for duplicate analysis
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' already exists for container '%s'", analysisNmae, containerName));
    _storage.saveAnalysis(containerName, analysis);
  }

  @Test
  public void testUpdateAnalysis() {
    Analysis analysis =
        new Analysis(
            "testanalysisupdate", Collections.singletonMap("testquestion", "testquestionvalue"));
    _storage.saveAnalysis("testcontainerupdate", analysis);
    Analysis savedAnalysis = _storage.getAnalysis("testcontainerupdate", "testanalysisupdate");
    savedAnalysis.addQuestion("modifiedquestion", "modifiedquestionvalue");
    Analysis updatedAnalysis = _storage.updateAnalysis("testcontainerupdate", savedAnalysis);

    //test that attributes were updated
    assertThat(updatedAnalysis, equalTo(savedAnalysis));
  }

  @Test
  public void testInvalidUpdateAnalysis() {
    Analysis newAnalysis =
        new Analysis(
            "testanalysisupdate", Collections.singletonMap("testquestion", "testquestionvalue"));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' doesn't exist for container '%s'",
            "testanalysisupdate", "testcontainer"));

    //test that update throws for non-existent analysis
    _storage.updateAnalysis("testcontainer", newAnalysis);
  }

  @Test
  public void testDeleteEmptyAnalysis() {
    Analysis analysis = new Analysis("testanalysis", new HashMap<>());
    _storage.saveAnalysis("testcontainer", analysis);
    _storage.deleteAnalysis("testcontainer", "testanalysis", false);
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' doesn't exist for container '%s'", "testanalysis", "testcontainer"));

    //should throw for the deleted analysis
    _storage.getAnalysis("testcontainer", "testanalysis");
  }

  @Test
  public void testDeleteNonEmptyAnalysis() {
    Analysis analysis =
        new Analysis(
            "testanalysis", Collections.singletonMap("testquestion", "testquestioncontent"));
    _storage.saveAnalysis("testcontainer", analysis);
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format("%s' is not empty, deletion must be forced", "testanalysis"));

    //Test that throws when analysis contains questions, with force=false
    _storage.deleteAnalysis("testcontainer", "testanalysis", false);
  }

  @Test
  public void testForceDeleteAnalysis() {
    Analysis analysis =
        new Analysis(
            "testanalysis", Collections.singletonMap("testquestion", "testquestioncontent"));
    _storage.saveAnalysis("testcontainer", analysis);
    assertTrue("Deletion Error", _storage.deleteAnalysis("testcontainer", "testanalysis", true));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(
        String.format(
            "Analysis '%s' doesn't exist for container '%s'", "testanalysis", "testcontainer"));

    //should throw for the deleted analysis
    _storage.getAnalysis("testcontainer", "testanalysis");
  }
}
