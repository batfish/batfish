package org.batfish.coordinator.resources;

import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;

/**
 * The {@link ReferenceBookResource} services client API calls for reference books. It is a
 * subresource of {@link ReferenceLibraryResource}.
 *
 * <p>This resource provides information about the book using GET and deletion using DELETE.
 *
 * <p>TODO: Support manipulation of subresources of the reference book (e.g., addressGroups)
 */
@Produces(MediaType.APPLICATION_JSON)
public class ReferenceBookResource {

  private BatfishLogger _logger = Main.getLogger();

  private String _bookName;
  private String _container;

  public ReferenceBookResource(String container, String book) {
    _container = container;
    _bookName = book;
  }

  @DELETE
  public Response delReferenceBook() {
    _logger.infof("WMS2: delReferenceBook '%s' '%s'\n", _container, _bookName);
    try {
      ReferenceLibrary library = Main.getWorkMgr().getReferenceLibrary(_container);
      library.delAddressBook(_bookName);
      ReferenceLibrary.write(library, Main.getWorkMgr().getReferenceLibraryPath(_container));
      return Response.ok().build();
    } catch (IOException e) {
      throw new InternalServerErrorException("Reference library is corrupted");
    } catch (NoSuchElementException e) {
      throw new NotFoundException("Book not found: " + _bookName);
    }
  }

  @GET
  public ReferenceBookBean getReferenceBook() {
    _logger.infof("WMS2: getReferenceBook: '%s' '%s'\n", _container, _bookName);
    try {
      return new ReferenceBookBean(
          Main.getWorkMgr()
              .getReferenceLibrary(_container)
              .getReferenceBook(_bookName)
              .orElseThrow(
                  () ->
                      new NotFoundException(
                          String.format(
                              "Reference book '%s' not found in container '%s'",
                              _bookName, _container))));
    } catch (IOException e) {
      throw new InternalServerErrorException("Reference library data is corrupted");
    }
  }

  /**
   * Puts a new {@link ReferenceBook} in the network's {@link ReferenceLibrary}. If one with the
   * same name exists, it is overwritten.
   */
  @PUT
  public Response putReferenceBook(ReferenceBookBean referenceBookBean) {
    _logger.infof("WMS2: putReferenceBook '%s'\n", _container);
    if (referenceBookBean.name == null) {
      throw new BadRequestException("ReferenceBook must have a name");
    }
    try {
      ReferenceLibrary.mergeReferenceBooks(
          Main.getWorkMgr().getReferenceLibraryPath(_container),
          ImmutableSortedSet.of(referenceBookBean.toAddressBook()));
      return Response.ok().build();
    } catch (IOException e) {
      throw new InternalServerErrorException("ReferenceLibrary resource is corrupted");
    }
  }
}
