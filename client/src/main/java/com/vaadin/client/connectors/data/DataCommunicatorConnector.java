/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.client.connectors.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.client.ServerConnector;
import com.vaadin.client.data.AbstractRemoteDataSource;
import com.vaadin.client.data.DataSource;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.server.data.DataCommunicator;
import com.vaadin.shared.data.DataCommunicatorClientRpc;
import com.vaadin.shared.data.DataCommunicatorConstants;
import com.vaadin.shared.data.DataRequestRpc;
import com.vaadin.shared.ui.Connect;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * A connector for DataCommunicator class.
 *
 * @since 8.0
 */
@Connect(DataCommunicator.class)
public class DataCommunicatorConnector extends AbstractExtensionConnector {

    /**
     * Client-side {@link DataSource} implementation to be used with
     * {@link DataCommunicator}.
     */
    public class VaadinDataSource extends AbstractRemoteDataSource<JsonObject> {

        private Set<String> droppedKeys = new HashSet<>();

        protected VaadinDataSource() {
            registerRpc(DataCommunicatorClientRpc.class,
                    new DataCommunicatorClientRpc() {

                        @Override
                        public void reset(int size) {
                            resetDataAndSize(size);
                        }

                        @Override
                        public void setData(int firstIndex, JsonArray data) {
                            ArrayList<JsonObject> rows = new ArrayList<>(
                                    data.length());
                            for (int i = 0; i < data.length(); i++) {
                                JsonObject rowObject = data.getObject(i);
                                rows.add(rowObject);
                            }

                            setRowData(firstIndex, rows);
                        }

                        @Override
                        public void updateData(JsonArray data) {
                            for (int i = 0; i < data.length(); ++i) {
                                updateRowData(data.getObject(i));
                            }
                        }
                    });
        }

        @Override
        protected void requestRows(int firstRowIndex, int numberOfRows,
                RequestRowsCallback<JsonObject> callback) {
            getRpcProxy(DataRequestRpc.class).requestRows(firstRowIndex,
                    numberOfRows, 0, 0);

            JsonArray dropped = Json.createArray();
            int i = 0;
            for (String key : droppedKeys) {
                dropped.set(i++, key);
            }
            droppedKeys.clear();

            getRpcProxy(DataRequestRpc.class).dropRows(dropped);
        }

        @Override
        public String getRowKey(JsonObject row) {
            return row.getString(DataCommunicatorConstants.KEY);
        }

        @Override
        protected void onDropFromCache(int rowIndex, JsonObject removed) {
            droppedKeys.add(getRowKey(removed));

            super.onDropFromCache(rowIndex, removed);
        }

        /**
         * Updates row data based on row key.
         *
         * @param row
         *            new row object
         */
        protected void updateRowData(JsonObject row) {
            int index = indexOfKey(getRowKey(row));
            if (index >= 0) {
                setRowData(index, Collections.singletonList(row));
            }
        }
    }

    private DataSource<JsonObject> ds = new VaadinDataSource();

    @Override
    protected void extend(ServerConnector target) {
        ServerConnector parent = getParent();
        if (parent instanceof HasDataSource) {
            ((HasDataSource) parent).setDataSource(ds);
        } else {
            assert false : "Parent not implementing HasDataSource";
        }
    }
}
