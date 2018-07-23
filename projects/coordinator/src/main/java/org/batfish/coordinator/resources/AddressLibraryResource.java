package org.batfish.coordinator.resources;

import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;
import org.batfish.role.addressbook.AddressLibrary;

/**
 * The {@link AddressLibraryResource} is a resource for servicing client API calls for the address
 * library. It is a subresource of {@link ContainerResource}.
 */
@Produces(MediaType.APPLICATION_JSON)
public class AddressLibraryResource {

  private BatfishLogger _logger = Main.getLogger();
  private String _container;

  public AddressLibraryResource(String container) {
    _container = container;
  }

  @POST
  public Response addAddressBook(AddressBookBean addressBookBean) {
    _logger.infof("WMS2: addAddressBook '%s'\n", _container);
    if (addressBookBean.name == null) {
      throw new BadRequestException("Address book must have a name");
    }
    try {
      AddressLibrary library = Main.getWorkMgr().getAddressLibrary(_container);
      if (library.getAddressBook(addressBookBean.name).isPresent()) {
        throw new BadRequestException("Duplicate bookname: " + addressBookBean.name);
      }
      AddressLibrary.mergeAddressBooks(
          Main.getWorkMgr().getAddressLibraryPath(_container),
          ImmutableSortedSet.of(addressBookBean.toAddressBook()));
      return Response.ok().build();
    } catch (IOException e) {
      throw new InternalServerErrorException("Address library resource is corrupted");
    }
  }

  /** Relocate the request to {@link AddressBookResource}. */
  @Path("/{book}")
  public AddressBookResource getAddressBookResource(@PathParam("book") String book) {
    return new AddressBookResource(_container, book);
  }

  /** Returns information about node roles in the container */
  @GET
  public AddressLibraryBean getAddressLibrary() {
    _logger.infof("WMS2: getAddressLibrary '%s'\n", _container);
    try {
      return new AddressLibraryBean(Main.getWorkMgr().getAddressLibrary(_container));
    } catch (IOException e) {
      throw new InternalServerErrorException("Node roles resource is corrupted");
    }
  }
}
