package ocr.predictor.db;

import com.datastax.driver.core.*;

/**
 * Created by dimuthuupeksha on 1/1/15.
 */
public class DBManager {


    private static Cluster cluster;
    private static Session session;

    static {
        connect("192.248.15.239");
    }


    public static void connect(String node) {
        cluster = Cluster.builder()
                .addContactPoint(node).build();
        Metadata metadata = cluster.getMetadata();
        System.out.printf("Connected to cluster: %s\n",
                metadata.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            System.out.printf("Datatacenter: %s; Host: %s; Rack: %s\n",
                    host.getDatacenter(), host.getAddress(), host.getRack());
        }
        session = cluster.connect();
    }


    public double getFinalWordProb(String word) {

        int endF = getWordCountInPositionReverse(0, word);

        int totF = getWordFrequency(word);
        double prob = 0;
        if (totF > 0) {
            prob = 1.0 * endF / totF;
        }

        return prob;

    }


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

        System.out.println(frequency);


        return frequency;
    }

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

}
