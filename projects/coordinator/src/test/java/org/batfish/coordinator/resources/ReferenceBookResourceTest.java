package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.version.BatfishVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ReferenceBookResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private Builder getReferenceBookTarget(String container, String bookName) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(container)
        .path(CoordConstsV2.RSC_REFERENCE_LIBRARY)
        .path(bookName)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Test
  public void delReferenceBook() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initNetwork(container, null);

    // write a library
    Main.getWorkMgr()
        .putReferenceLibrary(
            new ReferenceLibrary(ImmutableList.of(ReferenceBook.builder("book1").build())),
            container);

    try (Response response = getReferenceBookTarget(container, "book1").delete()) {
      // response should be OK and book1 should have disappeared
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }
    assertThat(
        Main.getWorkMgr().getReferenceLibrary(container).getReferenceBook("book1").isPresent(),
        equalTo(false));

    // deleting again should fail
    try (Response response2 = getReferenceBookTarget(container, "book1").delete()) {
      assertThat(response2.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void getReferenceBook() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initNetwork(container, null);

    // write a library to the right place
    Main.getWorkMgr()
        .putReferenceLibrary(
            new ReferenceLibrary(ImmutableList.of(ReferenceBook.builder("book1").build())),
            container);

    // we only check that the right type of object is returned at the expected URL target
    // we rely on ReferenceBookBean to have created the object with the right content
    try (Response response = getReferenceBookTarget(container, "book1").get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(ReferenceBook.class),
          equalTo(ReferenceBook.builder("book1").build()));
    }

    // should get 404 for non-existent dimension
    try (Response response2 = getReferenceBookTarget(container, "book2").get()) {
      assertThat(response2.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testPutReferenceBookMissingNetwork() {
    String network = "network1";
    String bookName = "book1";

    ReferenceBookBean book = new ReferenceBookBean(ReferenceBook.builder(bookName).build());
    try (Response response =
        getReferenceBookTarget(network, bookName)
            .put(Entity.entity(book, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void putReferenceBookSuccess() throws IOException {
    String network = "someContainer";
    String bookName = "book1";
    Main.getWorkMgr().initNetwork(network, null);

    // add book1
    ReferenceBookBean book = new ReferenceBookBean(ReferenceBook.builder(bookName).build());
    try (Response response =
        getReferenceBookTarget(network, bookName)
            .put(Entity.entity(book, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }
    // test: bookName should have been added
    ReferenceLibrary library = Main.getWorkMgr().getReferenceLibrary(network);
    assertThat(library.getReferenceBook(bookName).isPresent(), equalTo(true));

    // test: put of bookName again should succeed and the contents should be new
    AddressGroup ag = new AddressGroup(null, "ag");
    book.addressGroups = ImmutableSet.of(new AddressGroupBean(ag));
    try (Response response2 =
        getReferenceBookTarget(network, bookName)
            .put(Entity.entity(book, MediaType.APPLICATION_JSON))) {
      assertThat(response2.getStatus(), equalTo(OK.getStatusCode()));
    }
    ReferenceBook book2 =
        Main.getWorkMgr().getReferenceLibrary(network).getReferenceBook(bookName).get();
    assertThat(book2.getAddressGroups(), equalTo(ImmutableSet.of(ag)));
  }
}
