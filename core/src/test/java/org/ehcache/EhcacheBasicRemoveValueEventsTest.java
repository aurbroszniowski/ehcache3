/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ehcache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.ehcache.event.CacheEvent;
import org.ehcache.exceptions.CacheAccessException;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.ehcache.util.IsRemoved;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

/**
 * Provides testing of events for basic REMOVE(key, value) operations.
 *
 */
public class EhcacheBasicRemoveValueEventsTest extends EhcacheEventsTestBase {

  @Mock
  protected CacheLoaderWriter<String, String> cacheLoaderWriter;

  protected IsRemoved<String, String> isRemoved = new IsRemoved<String, String>();

  @Test
  public void testRemoveAllArgsNull() {
    final Ehcache<String, String> ehcache = getEhcache();

    try {
      ehcache.remove(null, null);
      fail();
    } catch (NullPointerException e) {
      // expected
    }
    verify(cacheEventListener, never()).onEvent(Matchers.<CacheEvent<String, String>>any());
  }

  @Test
  public void testRemoveKeyNotNullValueNull() throws Exception {
    final Ehcache<String, String> ehcache = getEhcache();

    try {
      ehcache.remove("key", null);
      fail();
    } catch (NullPointerException e) {
      // expected
    }
    verify(cacheEventListener, never()).onEvent(Matchers.<CacheEvent<String, String>>any());
  }

  @Test
  public void testRemoveKeyNullValueNotNull() throws Exception {
    final Ehcache<String, String> ehcache = getEhcache();
    try {
      ehcache.remove(null, "value");
      fail();
    } catch (NullPointerException e) {
      // expected
    }
    verify(cacheEventListener, never()).onEvent(Matchers.<CacheEvent<String, String>>any());
  }

  @Test
  public void testRemoveValueNoStoreEntryNoCacheLoaderWriter() throws Exception {
    buildStore(Collections.<String, String>emptyMap());
    final Ehcache<String, String> ehcache = getEhcache();
    assertFalse(ehcache.remove("key", "value"));
    verify(cacheEventListener,never()).onEvent(Matchers.<CacheEvent<String, String>>any());
  }

  @Test
  public void testRemoveValueEqualStoreEntryNoCacheLoaderWriter() throws Exception {
    buildStore(Collections.singletonMap("key", "value"));
    final Ehcache<String, String> ehcache = getEhcache();
    assertTrue(ehcache.remove("key", "value"));
    verify(cacheEventListener,times(1)).onEvent(argThat(isRemoved));
  }

  @Test
  public void testRemoveValueStoreThrows() throws Exception {
    buildStore(Collections.<String, String>emptyMap());
    doThrow(new CacheAccessException("")).when(store).compute(eq("key"), getAnyBiFunction(), getBooleanNullaryFunction());
    final Ehcache<String, String> ehcache = getEhcache();
    ehcache.remove("key", "value");
    verify(cacheEventListener,never()).onEvent(Matchers.<CacheEvent<String, String>>any());
  }

  private Ehcache<String, String> getEhcache() {
    return getEhcache("EhcacheBasicRemoveEventsTest");
  }
}