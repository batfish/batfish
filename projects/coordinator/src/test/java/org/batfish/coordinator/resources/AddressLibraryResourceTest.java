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
import org.batfish.role.addressbook.AddressBook;
import org.batfish.role.addressbook.AddressLibrary;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AddressLibraryResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private Builder getAddressLibraryTarget(String container) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(container)
        .path(CoordConstsV2.RSC_ADDRESS_LIBRARY)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Test
  public void addAddressBook() throws IOException {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);

    // add book1
    AddressBookBean book = new AddressBookBean(new AddressBook(null, "book1", null, null, null));
    Response response =
        getAddressLibraryTarget(container).post(Entity.entity(book, MediaType.APPLICATION_JSON));

    // test: book1 should have been added
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    AddressLibrary library = Main.getWorkMgr().getAddressLibrary(container);
    assertThat(library.getAddressBook("book1").isPresent(), equalTo(true));

    // test: another addition of book1 should fail
    Response response2 =
        getAddressLibraryTarget(container).post(Entity.entity(book, MediaType.APPLICATION_JSON));
    assertThat(response2.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  /** Test that we get back the address library */
  @Test
  public void getAddressLibrary() {
    String container = "someContainer";
    Main.getWorkMgr().initContainer(container, null);

    // we only check that the right type of object is returned at the expected URL target
    // we rely on AddressLibraryBean to have created the object with the right content
    Response response = getAddressLibraryTarget(container).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(AddressLibraryBean.class),
        equalTo(new AddressLibraryBean(new AddressLibrary(ImmutableList.of()))));
  }
}
