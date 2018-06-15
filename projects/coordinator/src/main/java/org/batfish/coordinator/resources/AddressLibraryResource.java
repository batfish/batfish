package org.batfish.coordinator.resources;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;

/**
 * The {@link AddressLibraryResource} is a resource for servicing client API calls for address
 * books. It is a subresource of {@link ContainerResource}.
 */
@Produces(MediaType.APPLICATION_JSON)
public class AddressLibraryResource {

  private BatfishLogger _logger = Main.getLogger();
  private String _container;
  private UriInfo _uriInfo;

  public AddressLibraryResource(UriInfo uriInfo, String container) {
    _container = container;
    _uriInfo = uriInfo;
  }

  @POST
  public Response addAddressBook(AddressBookBean addressBookBean) {
    _logger.infof("WMS2: addAddressBook '%s'\n", _container);
    if (addressBookBean.name == null) {
      throw new BadRequestException("Node role dimension must have a name");
    }
    // TODO: finish plumbing
    return Response.ok().build();
  }

  /** Relocate the request to {@link NodeRoleDimensionResource}. */
  @Path("/{book}")
  public AddressBookResource getAddressBookResource(@PathParam("book") String book) {
    return new AddressBookResource(_uriInfo, _container, book);
  }

  /** Returns information about node roles in the container */
  @GET
  public AddressLibraryBean getAddressLibrary() {
    _logger.infof("WMS2: getAddressLibrary '%s'\n", _container);
    // TODO: finish plumbing
    return null;
  }
}
