package org.batfish.role.addressbook;

import static org.batfish.role.addressbook.Library.checkDuplicates;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LibraryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  /** check that we barf on duplicates */
  @Test
  public void checkDuplicatesDups() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Duplicate");

    checkDuplicates("test", ImmutableList.of("a", "b", "a"));
  }

  /** check that we do NOT barf on duplicates */
  @Test
  public void checkDuplicatesNoDups() {
    checkDuplicates("test", ImmutableList.of(""));
    checkDuplicates("test", ImmutableList.of("a"));
    checkDuplicates("test", ImmutableList.of("a", "b", "c"));
  }

  /** check that we deserialize successfully and into two books */
  @Test
  public void libraryDeserialization() throws IOException {
    Library library =
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readResource("org/batfish/role/addressbook/libraryTwoBooks.json"),
                Library.class);

    assertThat(library.getBooks(), hasSize(2));
  }

  /** check that we barf on duplicate book names */
  @Test
  public void libraryDeserializationDuplicateBooks() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Duplicate");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/role/addressbook/libraryDuplicateBooks.json"),
            Library.class);
  }
}
