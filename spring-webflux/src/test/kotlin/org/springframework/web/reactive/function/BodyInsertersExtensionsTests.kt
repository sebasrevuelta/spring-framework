/*
 * Copyright 2002-2017 the original author or authors.
 *
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
 */

package org.springframework.web.reactive.function

import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.reactivestreams.Publisher


/**
 * Tests for [BodyExtractors] Kotlin extensions
 *
 * @author Sebastien Deleuze
 */
@RunWith(MockitoJUnitRunner::class)
class BodyInsertersExtensionsTests {

	@Test
	fun `bodyFromPublisher with reified type parameters`() {
		val publisher = mock<Publisher<Foo>>()
		assertNotNull(bodyFromPublisher(publisher))
	}

	@Test
	fun `bodyFromServerSentEvents with reified type parameters`() {
		val publisher = mock<Publisher<Foo>>()
		assertNotNull(bodyFromServerSentEvents(publisher))
	}

	class Foo
}
