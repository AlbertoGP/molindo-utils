/**
 * Copyright 2010 Molindo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.molindo.utils.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

public class SetMapTest {

	@Test
	public void test() {
		SetMap<String, String> map = SetMap.newSetMap();

		map.put("foo", "bar");
		map.putAll("foo", Arrays.asList("bar", "baz", "cux"));

		Set<String> set = map.get("foo");
		assertNotNull(set);
		assertEquals(3, set.size());

		assertTrue(set.containsAll(Arrays.asList("bar", "baz", "cux")));
	}
}