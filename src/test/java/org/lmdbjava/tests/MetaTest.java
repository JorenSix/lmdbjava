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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.lmdbjava.LmdbNativeException.PageCorruptedException.MDB_CORRUPTED;
import static org.lmdbjava.Meta.error;

import org.junit.Test;
import org.lmdbjava.Meta;
import org.lmdbjava.Meta.Version;

/**
 * Test {@link Meta}.
 */
public final class MetaTest {

  @Test
  public void coverPrivateConstructors() {
    TestUtils.invokePrivateConstructor(Meta.class);
  }

  @Test
  public void errCode() {
    assertThat(error(MDB_CORRUPTED), is(
               "MDB_CORRUPTED: Located page was wrong type"));
  }

  @Test
  public void version() {
    final Version v = Meta.version();
    assertThat(v, not(nullValue()));
    assertThat(v.major, is(0));
    assertThat(v.minor, is(9));
  }

}
