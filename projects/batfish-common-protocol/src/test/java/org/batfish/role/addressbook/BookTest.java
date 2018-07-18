package org.batfish.role.addressbook;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BookTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  /** check that we deserialize a basic object correctly */
  @Test
  public void bookDeserializationBasic() throws IOException {
    Book book =
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readResource("org/batfish/role/addressbook/bookBasic.json"), Book.class);

    assertThat(book.getAddressGroups(), hasSize(2));
    assertThat(book.getServiceEndpoints(), hasSize(2));
    assertThat(book.getServiceObjects(), hasSize(2));
    assertThat(book.getServiceObjectGroups(), hasSize(2));
  }

  /** check that we throw an error for duplicate address groups */
  @Test
  public void bookDeserializationDupAddressGroup() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Duplicate");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/role/addressbook/bookDupAddressGroup.json"),
            Book.class);
  }

  /** check that we throw an error when the same name is used in a service object and group */
  @Test
  public void bookDeserializationDupServiceName() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Duplicate");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/role/addressbook/bookDupServiceName.json"),
            Book.class);
  }

  /** check that we throw an error for undefined address groups */
  @Test
  public void bookDeserializationUndefAddressGroup() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Undefined");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/role/addressbook/bookUndefAddressGroup.json"),
            Book.class);
  }

  /** check that we throw an error for undefined service name */
  @Test
  public void bookDeserializationUndefServiceName() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Undefined");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/role/addressbook/bookUndefServiceName.json"),
            Book.class);
  }
}
