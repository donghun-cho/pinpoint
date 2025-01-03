/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.utils.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface JDBCDriverClass {
    Class<Driver> getDriver();

    default Driver newDriver() throws SQLException {
        final Class<Driver> driver = getDriver();
        try {
            return driver.getDeclaredConstructor().newInstance();
        } catch (Throwable th) {
            throw new SQLException(driver.getName() + " Driver create failed", th);
        }
    }

    Class<Connection> getConnection();

    Class<Statement> getStatement();

    Class<PreparedStatement> getPreparedStatement();

    Class<CallableStatement> getCallableStatement();
}
