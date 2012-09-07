/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package net.hydromatic.optiq.impl.splunk;

import net.hydromatic.optiq.MutableSchema;
import net.hydromatic.optiq.Schema;
import net.hydromatic.optiq.impl.splunk.search.SplunkConnection;
import net.hydromatic.optiq.jdbc.OptiqConnection;
import net.hydromatic.optiq.jdbc.UnregisteredDriver;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * JDBC driver for Splunk.
 *
 * <p>It accepts connect strings that start with "jdbc:splunk:".</p>
 *
 * @author jhyde
 */
public class SplunkDriver extends UnregisteredDriver {
    protected SplunkDriver() {
        super();
    }

    static {
        new SplunkDriver().register();
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith("jdbc:splunk:");
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Connection connection = super.connect(url, info);
        OptiqConnection optiqConnection = (OptiqConnection) connection;
        SplunkConnection splunkConnection;
        try {
            String url1 = info.getProperty("url");
            if (url1 == null) {
                throw new IllegalArgumentException(
                    "Must specify 'url' property");
            }
            URL url2 = new URL(url1);
            splunkConnection = new SplunkConnection(
                url2,
                info.getProperty("user"),
                info.getProperty("password"));
        } catch (Exception e) {
            throw new SQLException("Cannot connect", e);
        }
        final MutableSchema rootSchema = optiqConnection.getRootSchema();
        final String schemaName = "splunk";
        final SplunkSchema schema =
            new SplunkSchema(
                optiqConnection,
                splunkConnection,
                optiqConnection.getTypeFactory(),
                rootSchema.getSubSchemaExpression(
                    schemaName, Schema.class));
        rootSchema.addSchema(schemaName, schema);
        return connection;
    }
}

// End SplunkDriver.java