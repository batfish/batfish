package org.batfish.coordinator.resources;

import java.io.IOException;
import java.util.NoSuchElementException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;
import org.batfish.role.addressbook.AddressLibrary;

/**
 * The {@link AddressBookResource} services client API calls for address books. It is a subresource
 * of {@link AddressLibraryResource}.
 *
 * <p>This resource provides information about the book using GET and deletion using DELETE.
 *
 * <p>TODO: Support manipulation of subresources of the address book (e.g., addressGroups)
 */
@Produces(MediaType.APPLICATION_JSON)
public class AddressBookResource {

  private BatfishLogger _logger = Main.getLogger();
  private String _container;
  private String _bookName;

  public AddressBookResource(UriInfo uriInfo, String container, String book) {
    _container = container;
    _bookName = book;
  }

  @DELETE
  public Response delAddressBook() {
    _logger.infof("WMS2: delAddressBook '%s' '%s'\n", _container, _bookName);
    try {
      AddressLibrary addressLibrary = Main.getWorkMgr().getAddressLibrary(_container);
      addressLibrary.delAddressBook(_bookName);
      AddressLibrary.write(addressLibrary, Main.getWorkMgr().getAddressLibraryPath(_container));
      return Response.ok().build();
    } catch (IOException e) {
      throw new InternalServerErrorException("Address library is corrupted");
    } catch (NoSuchElementException e) {
      throw new NotFoundException("Book not found: " + _bookName);
    }
  }

  @GET
  public AddressBookBean getAddressBook() {
    _logger.infof("WMS2: getAddressBook: '%s' '%s'\n", _container, _bookName);
    try {
      return new AddressBookBean(
          Main.getWorkMgr()
              .getAddressLibrary(_container)
              .getAddressBook(_bookName)
              .orElseThrow(
                  () ->
                      new NotFoundException(
                          String.format(
                              "Address book '%s' not found in container '%s'",
                              _bookName, _container))));
    } catch (IOException e) {
      throw new InternalServerErrorException("Node roles resource is corrupted");
    }
  }
}
