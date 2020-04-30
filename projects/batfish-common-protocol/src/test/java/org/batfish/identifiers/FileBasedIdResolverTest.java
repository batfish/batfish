package org.batfish.identifiers;

import static org.batfish.identifiers.FileBasedIdResolver.listResolvableNames;
import static org.batfish.storage.FileBasedStorage.fromBase64;
import static org.batfish.storage.FileBasedStorage.toBase64;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test of {@link FileBasedIdResolver}. */
public final class FileBasedIdResolverTest {

  private FileBasedIdResolver _resolver;

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void setup() {
    _resolver = new FileBasedIdResolver(_folder.getRoot().toPath());
  }

  private static void assertLastComponentBase64Encoded(Path idPath) {
    assertNotNull(fromBase64(idPath.getFileName().toString()));
  }

  @Test
  public void testListResolvableNames() throws IOException {
    Path root = _folder.getRoot().toPath();
    Files.createFile(root.resolve(toBase64("foo.id")));
    Files.createFile(root.resolve(toBase64("bar.id")));
    Files.createFile(root.resolve("baz.id")); // should be excluded because it is not base64-encoded
    Files.createFile(
        root.resolve(toBase64("bath"))); // should be excluded because it doesn't end in ID

    assertThat(listResolvableNames(root), containsInAnyOrder("foo", "bar"));
  }

  @Test
  public void testGetAnalysisIdPath() {
    assertLastComponentBase64Encoded(
        _resolver.getAnalysisIdPath("analysis1", new NetworkId("net1_id")));
  }

  @Test
  public void testGetIssueSettingsIdPath() {
    assertLastComponentBase64Encoded(
        _resolver.getIssueSettingsIdPath("majorIssueType1", new NetworkId("net1_id")));
  }

  @Test
  public void testGetNetworkIdPath() {
    assertLastComponentBase64Encoded(_resolver.getNetworkIdPath("net1"));
  }

  @Test
  public void testGetQuestionIdPath() {
    // with analysis
    assertLastComponentBase64Encoded(
        _resolver.getQuestionIdPath(
            "question1", new NetworkId("net1_id"), new AnalysisId("analysis1_id")));

    // without analysis
    assertLastComponentBase64Encoded(
        _resolver.getQuestionIdPath("question1", new NetworkId("net1_id"), null));
  }

  @Test
  public void testGetQuestionSettingsIdPath() {
    assertLastComponentBase64Encoded(
        _resolver.getQuestionSettingsIdPath("questionClassId1", new NetworkId("net1_id")));
  }

  @Test
  public void testGetSnapshotIdPath() {
    assertLastComponentBase64Encoded(
        _resolver.getSnapshotIdPath("snapshot1", new NetworkId("net1_id")));
  }
}
