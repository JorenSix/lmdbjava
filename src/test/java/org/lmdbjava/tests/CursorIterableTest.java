/*-
 * #%L
 * LmdbJava
 * %%
 * Copyright (C) 2016 - 2022 The LmdbJava Open Source Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.lmdbjava.tests;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.Env.create;
import static org.lmdbjava.EnvFlags.MDB_NOSUBDIR;
import static org.lmdbjava.KeyRange.all;
import static org.lmdbjava.KeyRange.allBackward;
import static org.lmdbjava.KeyRange.atLeast;
import static org.lmdbjava.KeyRange.atLeastBackward;
import static org.lmdbjava.KeyRange.atMost;
import static org.lmdbjava.KeyRange.atMostBackward;
import static org.lmdbjava.KeyRange.closed;
import static org.lmdbjava.KeyRange.closedBackward;
import static org.lmdbjava.KeyRange.closedOpen;
import static org.lmdbjava.KeyRange.closedOpenBackward;
import static org.lmdbjava.KeyRange.greaterThan;
import static org.lmdbjava.KeyRange.greaterThanBackward;
import static org.lmdbjava.KeyRange.lessThan;
import static org.lmdbjava.KeyRange.lessThanBackward;
import static org.lmdbjava.KeyRange.open;
import static org.lmdbjava.KeyRange.openBackward;
import static org.lmdbjava.KeyRange.openClosed;
import static org.lmdbjava.KeyRange.openClosedBackward;
import static org.lmdbjava.PutFlags.MDB_NOOVERWRITE;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.primitives.UnsignedBytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lmdbjava.*;
import org.lmdbjava.CursorIterable.KeyVal;

/**
 * Test {@link CursorIterable}.
 */
public final class CursorIterableTest {

  @Rule
  public final TemporaryFolder tmp = new TemporaryFolder();
  private Dbi<ByteBuffer> db;
  private Env<ByteBuffer> env;
  private Deque<Integer> list;

  @After
  public void after() {
    env.close();
  }

  @Test
  public void allBackwardTest() {
    verify(allBackward(), 8, 6, 4, 2);
  }

  @Test
  public void allTest() {
    verify(all(), 2, 4, 6, 8);
  }

  @Test
  public void atLeastBackwardTest() {
    verify(atLeastBackward(TestUtils.bb(5)), 4, 2);
    verify(atLeastBackward(TestUtils.bb(6)), 6, 4, 2);
    verify(atLeastBackward(TestUtils.bb(9)), 8, 6, 4, 2);
  }

  @Test
  public void atLeastTest() {
    verify(atLeast(TestUtils.bb(5)), 6, 8);
    verify(atLeast(TestUtils.bb(6)), 6, 8);
  }

  @Test
  public void atMostBackwardTest() {
    verify(atMostBackward(TestUtils.bb(5)), 8, 6);
    verify(atMostBackward(TestUtils.bb(6)), 8, 6);
  }

  @Test
  public void atMostTest() {
    verify(atMost(TestUtils.bb(5)), 2, 4);
    verify(atMost(TestUtils.bb(6)), 2, 4, 6);
  }

  @Before
  public void before() throws IOException {
    final File path = tmp.newFile();
    env = create()
        .setMapSize(400 * 1024)
        .setMaxReaders(1)
        .setMaxDbs(1)
        .open(path, TestUtils.POSIX_MODE, MDB_NOSUBDIR);
    db = env.openDbi(TestUtils.DB_1, MDB_CREATE);
    list = new LinkedList<>();
    list.addAll(asList(2, 3, 4, 5, 6, 7, 8, 9));
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      final Cursor<ByteBuffer> c = db.openCursor(txn);
      c.put(TestUtils.bb(2), TestUtils.bb(3), MDB_NOOVERWRITE);
      c.put(TestUtils.bb(4), TestUtils.bb(5));
      c.put(TestUtils.bb(6), TestUtils.bb(7));
      c.put(TestUtils.bb(8), TestUtils.bb(9));
      txn.commit();
    }
  }

  @Test
  public void closedBackwardTest() {
    verify(closedBackward(TestUtils.bb(7), TestUtils.bb(3)), 6, 4);
    verify(closedBackward(TestUtils.bb(6), TestUtils.bb(2)), 6, 4, 2);
    verify(closedBackward(TestUtils.bb(9), TestUtils.bb(3)), 8, 6, 4);
  }

  @Test
  public void closedOpenBackwardTest() {
    verify(closedOpenBackward(TestUtils.bb(8), TestUtils.bb(3)), 8, 6, 4);
    verify(closedOpenBackward(TestUtils.bb(7), TestUtils.bb(2)), 6, 4);
    verify(closedOpenBackward(TestUtils.bb(9), TestUtils.bb(3)), 8, 6, 4);
  }

  @Test
  public void closedOpenTest() {
    verify(closedOpen(TestUtils.bb(3), TestUtils.bb(8)), 4, 6);
    verify(closedOpen(TestUtils.bb(2), TestUtils.bb(6)), 2, 4);
  }

  @Test
  public void closedTest() {
    verify(closed(TestUtils.bb(3), TestUtils.bb(7)), 4, 6);
    verify(closed(TestUtils.bb(2), TestUtils.bb(6)), 2, 4, 6);
    verify(closed(TestUtils.bb(1), TestUtils.bb(7)), 2, 4, 6);
  }

  @Test
  public void greaterThanBackwardTest() {
    verify(greaterThanBackward(TestUtils.bb(6)), 4, 2);
    verify(greaterThanBackward(TestUtils.bb(7)), 6, 4, 2);
    verify(greaterThanBackward(TestUtils.bb(9)), 8, 6, 4, 2);
  }

  @Test
  public void greaterThanTest() {
    verify(greaterThan(TestUtils.bb(4)), 6, 8);
    verify(greaterThan(TestUtils.bb(3)), 4, 6, 8);
  }

  @Test(expected = IllegalStateException.class)
  public void iterableOnlyReturnedOnce() {
    try (Txn<ByteBuffer> txn = env.txnRead();
         CursorIterable<ByteBuffer> c = db.iterate(txn)) {
      c.iterator(); // ok
      c.iterator(); // fails
    }
  }

  @Test
  public void iterate() {
    try (Txn<ByteBuffer> txn = env.txnRead();
         CursorIterable<ByteBuffer> c = db.iterate(txn)) {
      for (final KeyVal<ByteBuffer> kv : c) {
        assertThat(kv.key().getInt(), is(list.pollFirst()));
        assertThat(kv.val().getInt(), is(list.pollFirst()));
      }
    }
  }

  @Test(expected = IllegalStateException.class)
  public void iteratorOnlyReturnedOnce() {
    try (Txn<ByteBuffer> txn = env.txnRead();
         CursorIterable<ByteBuffer> c = db.iterate(txn)) {
      c.iterator(); // ok
      c.iterator(); // fails
    }
  }

  @Test
  public void lessThanBackwardTest() {
    verify(lessThanBackward(TestUtils.bb(5)), 8, 6);
    verify(lessThanBackward(TestUtils.bb(2)), 8, 6, 4);
  }

  @Test
  public void lessThanTest() {
    verify(lessThan(TestUtils.bb(5)), 2, 4);
    verify(lessThan(TestUtils.bb(8)), 2, 4, 6);
  }

  @Test(expected = NoSuchElementException.class)
  public void nextThrowsNoSuchElementExceptionIfNoMoreElements() {
    try (Txn<ByteBuffer> txn = env.txnRead();
         CursorIterable<ByteBuffer> c = db.iterate(txn)) {
      final Iterator<KeyVal<ByteBuffer>> i = c.iterator();
      while (i.hasNext()) {
        final KeyVal<ByteBuffer> kv = i.next();
        assertThat(kv.key().getInt(), is(list.pollFirst()));
        assertThat(kv.val().getInt(), is(list.pollFirst()));
      }
      assertThat(i.hasNext(), is(false));
      i.next();
    }
  }

  @Test
  public void openBackwardTest() {
    verify(openBackward(TestUtils.bb(7), TestUtils.bb(2)), 6, 4);
    verify(openBackward(TestUtils.bb(8), TestUtils.bb(1)), 6, 4, 2);
    verify(openBackward(TestUtils.bb(9), TestUtils.bb(4)), 8, 6);
  }

  @Test
  public void openClosedBackwardTest() {
    verify(openClosedBackward(TestUtils.bb(7), TestUtils.bb(2)), 6, 4, 2);
    verify(openClosedBackward(TestUtils.bb(8), TestUtils.bb(4)), 6, 4);
    verify(openClosedBackward(TestUtils.bb(9), TestUtils.bb(4)), 8, 6, 4);
  }

  @Test
  public void openClosedBackwardTestWithGuava() {
    final Comparator<byte[]> guava = UnsignedBytes.lexicographicalComparator();
    final Comparator<ByteBuffer> comparator = (bb1, bb2) -> {
      final byte[] array1 = new byte[bb1.remaining()];
      final byte[] array2 = new byte[bb2.remaining()];
      bb1.mark();
      bb2.mark();
      bb1.get(array1);
      bb2.get(array2);
      bb1.reset();
      bb2.reset();
      return guava.compare(array1, array2);
    };
    verify(openClosedBackward(TestUtils.bb(7), TestUtils.bb(2)), comparator, 6, 4, 2);
    verify(openClosedBackward(TestUtils.bb(8), TestUtils.bb(4)), comparator, 6, 4);
  }

  @Test
  public void openClosedTest() {
    verify(openClosed(TestUtils.bb(3), TestUtils.bb(8)), 4, 6, 8);
    verify(openClosed(TestUtils.bb(2), TestUtils.bb(6)), 4, 6);
  }

  @Test
  public void openTest() {
    verify(open(TestUtils.bb(3), TestUtils.bb(7)), 4, 6);
    verify(open(TestUtils.bb(2), TestUtils.bb(8)), 4, 6);
  }

  @Test
  public void removeOddElements() {
    verify(all(), 2, 4, 6, 8);
    int idx = -1;
    try (Txn<ByteBuffer> txn = env.txnWrite()) {
      try (CursorIterable<ByteBuffer> ci = db.iterate(txn)) {
        final Iterator<KeyVal<ByteBuffer>> c = ci.iterator();
        while (c.hasNext()) {
          c.next();
          idx++;
          if (idx % 2 == 0) {
            c.remove();
          }
        }
      }
      txn.commit();
    }
    verify(all(), 4, 8);
  }

  private void verify(final KeyRange<ByteBuffer> range, final int... expected) {
    verify(range, null, expected);
  }

  private void verify(final KeyRange<ByteBuffer> range,
                      final Comparator<ByteBuffer> comparator,
                      final int... expected) {
    final List<Integer> results = new ArrayList<>();

    try (Txn<ByteBuffer> txn = env.txnRead();
         CursorIterable<ByteBuffer> c = db.iterate(txn, range, comparator)) {
      for (final KeyVal<ByteBuffer> kv : c) {
        final int key = kv.key().getInt();
        final int val = kv.val().getInt();
        results.add(key);
        assertThat(val, is(key + 1));
      }
    }

    assertThat(results, hasSize(expected.length));
    for (int idx = 0; idx < results.size(); idx++) {
      assertThat(results.get(idx), is(expected[idx]));
    }
  }

}
