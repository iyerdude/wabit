/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of Wabit.
 *
 * Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit.enterprise.client;

import java.util.Set;

import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;

public class GrantTest extends AbstractWabitObjectTest {
	
	private Grant grant;
	
	@Override
	public Set<String> getPropertiesToIgnoreForPersisting() {
		Set<String> ignored = super.getPropertiesToIgnoreForPersisting();
		ignored.add("name");
		return ignored;
	}
	
	@Override
	protected void setUp() throws Exception {
		grant = new Grant("Something", null, true, true, true, true, true);
	}

	@Override
	public WabitObject getObjectUnderTest() {
		return grant;
	}

}
