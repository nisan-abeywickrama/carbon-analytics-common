/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.dataservice;

import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordReader;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the analytics data service operations.
 */
public interface AnalyticsDataService extends AnalyticsRecordReader {
    
    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are not case sensitive.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to be created
     * @throws AnalyticsException
     */
    void createTable(int tenantId, String tableName) throws AnalyticsException;
    
    /**
     * Creates a table, if not already there, where the columns are not defined here, but can contain any arbitrary number
     * of columns when data is added. The table names are not case sensitive.
     * @param tenantId The tenant which this table belongs to
     * @param recordStoreName The name of the target record store to store the table at
     * @param tableName The name of the table to be created
     * @throws AnalyticsException
     */
    void createTable(int tenantId, String recordStoreName, String tableName) throws AnalyticsException;

    /**
     * Clears the index data of the table. This will delete all the index information
     * up to the current moment.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to clear the index data from
     * @throws AnalyticsException
     */
    void clearIndexData(int tenantId, String tableName) throws AnalyticsIndexException;

    /**
     * Sets the schema for the target analytics table, if there is already one assigned, it will be 
     * overwritten.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param schema The schema to be applied to the table
     * @throws AnalyticsTableNotAvailableException
     * @throws AnalyticsException
     */
    void setTableSchema(int tenantId, String tableName, 
            AnalyticsSchema schema) throws AnalyticsTableNotAvailableException, AnalyticsException;
    
    /**
     * Retrieves the table schema for the given table.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @return The schema of the table
     * @throws AnalyticsTableNotAvailableException
     * @throws AnalyticsException
     */
    AnalyticsSchema getTableSchema(int tenantId, String tableName) 
            throws AnalyticsTableNotAvailableException, AnalyticsException;
    
    /**
     * Checks if the specified table with the given category and name exists.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The table name
     * @return true if the table exists, false otherwise
     * @throws AnalyticsException
     */
    boolean tableExists(int tenantId, String tableName) throws AnalyticsException;
    
    /**
     * Deletes the table with the given category and name if a table exists already.
     * This will not throw an error if the table is not there.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to be dropped
     * @throws AnalyticsException
     */
    void deleteTable(int tenantId, String tableName) throws AnalyticsException;
    
    /**
     * Lists all the current tables with the given category.
     * @param tenantId The tenant which this table belongs to
     * @return The list of table names
     * @throws AnalyticsException
     */
    List<String> listTables(int tenantId) throws AnalyticsException;

    /**
     * Returns the number of records in the table with the given category and name.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to get the count from
     * @param timeFrom The starting time to consider the count from, inclusive, relatively to epoch,
     * Long.MIN_VALUE should signal, this restriction to be disregarded
     * @param timeTo The ending time to consider the count to, non-inclusive, relatively to epoch,
     * Long.MAX_VALUE should signal, this restriction to be disregarded     
     * @return The record count
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    
    /**
     * Adds a new record to the table. If the record id is mentioned, 
     * it will be used to do the insert, or else, it will check the table's schema to check for the existence of
     * primary keys, if there are any, the primary keys will be used to derive the id, or else
     * the insert will be done with a randomly generated id.
     * If the record already exists, it updates the record store with the given records, matches by its record id, 
     * this will be a full replace of the record, where the older record is effectively deleted and the new one is
     * added, there will not be a merge of older record's field's with the new one.
     * @param records The list of records to be inserted
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Retrieves data from a table, with a given range.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns The list of columns to required in results, null if all needs to be returned
     * @param timeFrom The starting time to get records from, inclusive, relatively to epoch,
     * Long.MIN_VALUE should signal, this restriction to be disregarded
     * @param timeTo The ending time to get records to, non-inclusive, relatively to epoch,
     * Long.MAX_VALUE should signal, this restriction to be disregarded
     * @param recordsFrom The paginated index from value, zero based, inclusive
     * @param recordsCount The paginated records count to be read, -1 for infinity
     * @return An array of {@link RecordGroup} objects, which represents individual data sets in their local location
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
            long timeTo, int recordsFrom, int recordsCount) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Retrieves data from a table with given ids.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns The list of columns to required in results, null if all needs to be returned
     * @param ids The list of ids of the records to be read
     * @return An array of {@link RecordGroup} objects, which contains individual data sets in their local location
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Retrieves data from a table with the values for all the given primary key value composites.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on
     * @param numPartitionsHint The best effort number of splits this should return
     * @param columns The list of columns to required in results, null if all needs to be returned 
     * @param valuesBatch A batch of key/values which contains the primary keys values to match the data in the table
     * @return An array of {@link RecordGroup} objects, which contains individual data sets in their local location
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    RecordGroup[] getWithKeyValues(int tenantId, String tableName, int numPartitionsHint, List<String> columns, 
            List<Map<String, Object>> valuesBatch) throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Checks whether or not pagination (i.e. jumping to record n and then retrieving k further records)
     * is supported by the underlying record store implementation.
     * Also returns false if the total record count in a table cannot be determined.
     *
     * @return Pagination/row-count support
     */
    boolean isPaginationSupported();

    /**
     * Deletes a set of records in the table.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on 
     * @param timeFrom The starting time to get records from for deletion
     * @param timeTo The ending time to get records to for deletion
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    void delete(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;
    
    /**
     * Delete data in a table with given ids.
     * @param tenantId The tenant which this table belongs to
     * @param tableName The name of the table to search on 
     * @param ids The list of ids of the records to be deleted
     * @throws AnalyticsException
     * @throws AnalyticsTableNotAvailableException
     */
    void delete(int tenantId, String tableName, List<String> ids) 
            throws AnalyticsException, AnalyticsTableNotAvailableException;

    /**
     * Searches the data with a given search query.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param query The search query
     * @param start The start location of the result, 0 based
     * @param count The maximum number of result entries to be returned
     * @return A list of {@link SearchResultEntry}s
     * @throws AnalyticsIndexException
     * @throws AnalyticsException 
     */
    List<SearchResultEntry> search(int tenantId, String tableName,
            String query, int start, int count) throws AnalyticsIndexException, AnalyticsException;
    
    /**
     * Returns the search count of results of a given search query.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param query The search query
     * @return The count of results
     * @throws AnalyticsIndexException
     */
    int searchCount(int tenantId, String tableName, String query) throws AnalyticsIndexException;

    /**
     * Returns the drill down results of a search query, given
     * {@link org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest}
     * @param tenantId The tenant id
     * @param drillDownRequest The drilldown object which contains the drilldown information
     * @return the results containing the ids which match the drilldown query.
     * @throws AnalyticsIndexException
     */
    public List<SearchResultEntry> drillDownSearch(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException;

    /**
     * Returns the count of results of a search query, given
     * {@link org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest}
     * @param tenantId The tenant id
     * @param drillDownRequest The drilldown object which contains the drilldown information
     * @return the count of the records which match the drilldown query
     * @throws AnalyticsIndexException
     */
    public double drillDownSearchCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException;

    /**
     * Returns the subcategories of a facet field, given
     * {@link org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest}
     * @param tenantId The tenant id
     * @param drillDownRequest The category drilldown object which contains the category drilldown information
     * @return
     * @throws AnalyticsIndexException
     */
    public SubCategories drillDownCategories(int tenantId, CategoryDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException;

    /**
     * Returns a list of range buckets of a specific field with the total score for each bucket.
     * @param tenantId The tenant id
     * @param drillDownRequest The drilldown request which contains all query information
     * @return A list of {@link org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange}
     * with score property not-null
     * @throws AnalyticsIndexException
     */
    public List<AnalyticsDrillDownRange> drillDownRangeCount(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
        throws AnalyticsIndexException;
    /**
     * This method waits until the current indexing operations for the system is done.
     * @param maxWait Maximum amount of time in milliseconds, if the time is reached, 
     * an {@link AnalyticsTimeoutException} will be thrown, -1 for infinity
     * @throws AnalyticsTimeoutException
     * @throws AnalyticsException 
     */
    public void waitForIndexing(long maxWait) throws AnalyticsTimeoutException, AnalyticsException;
    
    /**
     * Destroys and frees any resources taken up by the analytics data service implementation.
     */
    void destroy() throws AnalyticsException;
    
}
