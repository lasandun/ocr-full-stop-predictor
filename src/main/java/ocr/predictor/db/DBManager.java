/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package ocr.predictor.db;

import com.datastax.driver.core.*;
import ocr.predictor.SysProperty;
import org.apache.log4j.Logger;

/**
 * Database manager which requests data from Cassandra database
 * @author Upeksha
 */
public class DBManager {

    private static Cluster cluster;
    private static Session session;
    private final static Logger logger = Logger.getLogger(DBManager.class);

    static {
        connect("192.248.15.239");
    }

    public static void connect(String node) {
        String username = SysProperty.getProperty("username");
        String password = SysProperty.getProperty("password");
        cluster = Cluster.builder().addContactPoint(node).withCredentials(username, password).build();
        Metadata metadata = cluster.getMetadata();
        logger.info("Connected to cluster: " + metadata.getClusterName() + "\n");
        for (Host host : metadata.getAllHosts()) {
            logger.info("Datacentre: " + host.getDatacenter());
            logger.info("host : " + host.getAddress());
            logger.info("Rank : " + host.getRack());
        }
        session = cluster.connect();
    }

    // get probability of a word being the last word of a sentence
    public double getFinalWordProb(String word) {
        int endF = getWordCountInPositionReverse(0, word);
        int totF = getWordFrequency(word);
        double prob = 0;
        if (totF > 0) {
            prob = 1.0 * endF / totF;
        }
        return prob;
    }

    // get the occurences the word being at the end of a sentence
    public int getWordCountInPositionReverse(int position, String word) {
        PreparedStatement query;
        com.datastax.driver.core.ResultSet results;
        int frequency = 0;

        query = session.prepare(
                "select frequency from corpus.word_inv_pos_id WHERE inv_position=? AND word=?");
        results = session.execute(query.bind(position, word));
        for (Row row : results) {
            frequency += row.getInt("frequency");
            break;
        }
        logger.info(frequency);
        return frequency;
    }

    //  get the frequency of the given word from Cassandra DB
    public int getWordFrequency(String word) {
        PreparedStatement query = session.prepare(
                "select frequency from corpus.word_frequency WHERE word=?");
        com.datastax.driver.core.ResultSet results = session.execute(query.bind(word));

        int freq = 0;
        for (Row row : results) {
            System.out.format("%d\n", row.getInt("frequency"));
            freq = (row.getInt("frequency"));
        }
        return freq;
    }
    
    public void closeConnection() {
        session.close();
        cluster.close();
    }

}
