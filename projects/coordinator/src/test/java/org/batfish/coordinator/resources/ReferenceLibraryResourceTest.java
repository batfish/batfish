package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
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

public class ReferenceLibraryResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private Builder getReferenceLibraryTarget(String container) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(container)
        .path(CoordConstsV2.RSC_REFERENCE_LIBRARY)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Test
  public void addReferenceBook() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initNetwork(container, null);

    // add book1
    ReferenceBookBean book = new ReferenceBookBean(ReferenceBook.builder("book1").build());
    Response response =
        getReferenceLibraryTarget(container).post(Entity.entity(book, MediaType.APPLICATION_JSON));

    // test: book1 should have been added
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    ReferenceLibrary library = Main.getWorkMgr().getReferenceLibrary(container);
    assertThat(library.getReferenceBook("book1").isPresent(), equalTo(true));

    // test: another addition of book1 should fail
    Response response2 =
        getReferenceLibraryTarget(container).post(Entity.entity(book, MediaType.APPLICATION_JSON));
    assertThat(response2.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  /** Test that we get back the reference library */
  @Test
  public void getReferenceLibrary() {
    String container = "someContainer";
    Main.getWorkMgr().initNetwork(container, null);

    // we only check that the right type of object is returned at the expected URL target
    // we rely on ReferenceLibraryBean to have created the object with the right content
    Response response = getReferenceLibraryTarget(container).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(ReferenceLibraryBean.class),
        equalTo(new ReferenceLibraryBean(new ReferenceLibrary(ImmutableList.of()))));
  }
}
