package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.Version;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ReferenceBookResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private Builder getAddressBookTarget(String container, String bookName) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(container)
        .path(CoordConstsV2.RSC_REFERENCE_LIBRARY)
        .path(bookName)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Test
  public void delAddressBook() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initNetwork(container, null);

    // write a library to the right place
    ReferenceLibrary.write(
        new ReferenceLibrary(ImmutableList.of(ReferenceBook.builder("book1").build())),
        Main.getWorkMgr().getReferenceLibraryPath(container));

    Response response = getAddressBookTarget(container, "book1").delete();

    // response should be OK and book1 should have disappeared
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        Main.getWorkMgr().getReferenceLibrary(container).getReferenceBook("book1").isPresent(),
        equalTo(false));

    // deleting again should fail
    Response response2 = getAddressBookTarget(container, "book1").delete();
    assertThat(response2.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void getAddressBook() throws JsonProcessingException {
    String container = "someContainer";
    Main.getWorkMgr().initNetwork(container, null);

    // write a library to the right place
    ReferenceLibrary.write(
        new ReferenceLibrary(ImmutableList.of(ReferenceBook.builder("book1").build())),
        Main.getWorkMgr().getReferenceLibraryPath(container));

    // we only check that the right type of object is returned at the expected URL target
    // we rely on ReferenceBookBean to have created the object with the right content
    Response response = getAddressBookTarget(container, "book1").get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(ReferenceBook.class), equalTo(ReferenceBook.builder("book1").build()));

    // should get 404 for non-existent dimension
    Response response2 = getAddressBookTarget(container, "book2").get();
    assertThat(response2.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }
}
