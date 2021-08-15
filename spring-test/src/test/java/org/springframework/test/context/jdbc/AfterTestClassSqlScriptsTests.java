/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.test.context.jdbc;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import org.springframework.core.Ordered;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.transaction.TestContextTransactionUtils;

@SpringJUnitConfig(PopulatedSchemaDatabaseConfig.class)
@DirtiesContext
@Sql(value = {"drop-schema.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@TestExecutionListeners(
		value = AfterTestClassSqlScriptsTests.VerifyTestExecutionListener.class,
		mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class AfterTestClassSqlScriptsTests extends AbstractTransactionalTests {

	@Test
	@Sql(scripts = "data-add-catbert.sql")
	void databaseHasBeenInitialized() {
		// Ensure that the database has been initialized and can be accessed.
		assertUsers("Catbert");
	}

	static class VerifyTestExecutionListener implements TestExecutionListener, Ordered {

		@Override
		public void afterTestClass(TestContext testContext) throws Exception {
			DataSource dataSource = TestContextTransactionUtils.retrieveDataSource(testContext, null);
			try {
				new JdbcTemplate(dataSource).queryForList("SELECT name FROM user", String.class);
				throw new AssertionError("BadSqlGrammarException should have been thrown.");
			}
			catch (BadSqlGrammarException expected) {
			}
		}

		@Override
		public int getOrder() {
			// Must run before DirtiesContextTestExecutionListener. Otherwise, the old data source will be removed and
			// replaced with a new one.
			return 3001;
		}
	}
}
