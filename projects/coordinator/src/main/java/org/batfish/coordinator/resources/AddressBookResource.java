package org.batfish.coordinator.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;

/**
 * The {@link NodeRoleDimensionResource} is a resource for servicing client API calls for node role
 * dimensions. It is a subresource of {@link NodeRolesResource}.
 *
 * <p>This resource provides information about the role dimension using GET. It also allows
 * modifications and creating of new dimensions using PUT.
 */
@Produces(MediaType.APPLICATION_JSON)
public class AddressBookResource {

  private BatfishLogger _logger = Main.getLogger();
  private String _container;
  private String _book;

  public AddressBookResource(UriInfo uriInfo, String container, String book) {
    _container = container;
    _book = book;
  }

  @DELETE
  public Response delAddressBook() {
    _logger.infof("WMS2: delAddressBook '%s' '%s'\n", _container, _book);
    // TODO: finish the plumbing
    return Response.ok().build();
  }

  @GET
  public AddressBookBean getAddressBook() {
    _logger.infof("WMS2: getAddressBook: '%s' '%s'\n", _container, _book);
    // TODO: finish the plumbing
    return null;
  }
}
