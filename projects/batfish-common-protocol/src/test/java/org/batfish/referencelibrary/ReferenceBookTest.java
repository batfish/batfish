package org.batfish.referencelibrary;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link ReferenceBook} */
public class ReferenceBookTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  /** check that we deserialize a basic object correctly */
  @Test
  public void bookDeserializationBasic() throws IOException {
    ReferenceBook book =
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readResource("org/batfish/referencelibrary/bookBasic.json"),
                ReferenceBook.class);

    assertThat(book.getAddressGroups(), hasSize(2));
    assertThat(book.getFilterGroups(), hasSize(2));
    assertThat(book.getInterfaceGroups(), hasSize(2));
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
            CommonUtil.readResource("org/batfish/referencelibrary/bookDupAddressGroup.json"),
            ReferenceBook.class);
  }

  /** check that we throw an error when the same name is used in a service object and group */
  @Test
  public void bookDeserializationDupServiceName() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Duplicate");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/referencelibrary/bookDupServiceName.json"),
            ReferenceBook.class);
  }

  /** check that we throw an error for undefined address groups */
  @Test
  public void bookDeserializationUndefAddressGroup() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Undefined");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/referencelibrary/bookUndefAddressGroup.json"),
            ReferenceBook.class);
  }

  /** check that we throw an error for undefined service name */
  @Test
  public void bookDeserializationUndefServiceName() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Undefined");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/referencelibrary/bookUndefServiceName.json"),
            ReferenceBook.class);
  }

  @Test
  public void testAddGroupAndDescendantNamesNonExistentGroup() {
    _thrown.expect(NoSuchElementException.class);
    ReferenceBook book = ReferenceBook.builder("book").build();
    Set<String> groupNames = new HashSet<>();
    book.addGroupAndDescendantNames("a", groupNames);
  }

  @Test
  public void testAddGroupAndDescendantNamesCycles() {
    // a -> b -> a
    ReferenceBook book =
        ReferenceBook.builder("book")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup("a", null, ImmutableSortedSet.of("b")),
                    new AddressGroup("b", null, ImmutableSortedSet.of("a"))))
            .build();
    Set<String> groupNames = new HashSet<>();
    book.addGroupAndDescendantNames("a", groupNames);
    assertThat(groupNames, equalTo(ImmutableSet.of("a", "b")));
  }

  @Test
  public void testAddGroupAndDescendantNamesNoChildren() {
    ReferenceBook book =
        ReferenceBook.builder("book")
            .setAddressGroups(ImmutableList.of(new AddressGroup("a", null, null)))
            .build();
    Set<String> groupNames = new HashSet<>();
    book.addGroupAndDescendantNames("a", groupNames);
    assertThat(groupNames, equalTo(ImmutableSet.of("a")));
  }

  @Test
  public void testAddGroupAndDescendantNamesDeepChildren() {
    ReferenceBook book =
        ReferenceBook.builder("book")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup("a", null, ImmutableSortedSet.of("b", "c")),
                    new AddressGroup("b", null, ImmutableSortedSet.of("d")),
                    new AddressGroup("c", null, null),
                    new AddressGroup("d", null, null)))
            .build();
    Set<String> groupNames = new HashSet<>();
    book.addGroupAndDescendantNames("a", groupNames);
    assertThat(groupNames, equalTo(ImmutableSet.of("a", "b", "c", "d")));
  }

  @Test
  public void testGetAddressesRecursive() {
    ReferenceBook book =
        ReferenceBook.builder("book")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup(
                        "a", ImmutableSortedSet.of("1.1.1.1"), ImmutableSortedSet.of("b", "c")),
                    new AddressGroup("b", ImmutableSortedSet.of("2.2.2.2"), null),
                    new AddressGroup("c", null, null)))
            .build();
    // should include addresses from a and b; no addresses in c shouldn't matter
    assertThat(book.getAddressesRecursive("a"), equalTo(ImmutableSet.of("1.1.1.1", "2.2.2.2")));
  }
}
