/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.time.ZoneId;

import org.springframework.util.StringUtils;

/**
 * Editor for {@code java.time.ZoneId}, translating zone ID Strings into {@code ZoneId}
 * objects. Exposes the {@code TimeZone} ID as a text representation.
 *
 * @author Nicholas Williams
 * @author Sam Brannen
 * @author Ngoc Nhan
 * @since 4.0
 * @see java.time.ZoneId
 * @see TimeZoneEditor
 */
public class ZoneIdEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			text = text.trim();
		}
		try {

			setValue(ZoneId.of(text));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Failed to convert ZoneId for " + text, ex);
		}
	}

	@Override
	public String getAsText() {
		ZoneId value = (ZoneId) getValue();
		return (value != null ? value.getId() : "");
	}

}
