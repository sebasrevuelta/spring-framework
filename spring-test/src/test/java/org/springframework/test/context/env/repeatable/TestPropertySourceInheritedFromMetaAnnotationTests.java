/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.test.context.env.repeatable;


import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestPropertySource(properties = "key = 051187")
@AnnotationWithTestProperty
public class TestPropertySourceInheritedFromMetaAnnotationTests {

	@Autowired
	Environment env;

	@Value("${key}")
	String key;

	@Value("${meta}")
	String meta;


	@Test
	public void inlineLocalPropertyAndPropertyFromMetaAnnotation() {
		// local inlined:
		assertThat(env.getProperty("key")).isEqualTo("051187");
		assertThat(this.key).isEqualTo("051187");
		// inlined from meta-annotation:
		assertThat(env.getProperty("meta")).isEqualTo("value from meta-annotation");
		assertThat(meta).isEqualTo("value from meta-annotation");
	}


	@Configuration
	static class Config {
	}
}
