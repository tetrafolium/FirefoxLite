/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.web;

import static org.junit.Assert.*;

import org.junit.Test;

public class BrowsingSessionTest {
  @Test
  public void testSingleton() {
    assertNotNull(BrowsingSession.getInstance());

    assertEquals(BrowsingSession.getInstance(), BrowsingSession.getInstance());
  }
}