package ocr.predictor.db;

import oracle.jdbc.OracleCallableStatement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by dimuthuupeksha on 1/1/15.
 */
public class DBManager {
    private static final String DB_CONNECTION = "jdbc:oracle:thin:@//192.248.15.239:1522/corpus.sinmin.com";
    private static final String DB_USER = "sinmin";
    private static final String DB_PASSWORD = "Sinmin1234";

    private static Connection dbConnection = null;

    private static Connection createDBConnection() {

        if (dbConnection == null) {
            try {
                dbConnection = DriverManager.getConnection(
                        DB_CONNECTION, DB_USER, DB_PASSWORD);
                return dbConnection;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dbConnection;
    }

    public double getFinalWordProb(String word) {
        try {
            createDBConnection();
            String sql = "select endF,totF from (select  count(*) as endF from word w, sentence_word sw, sentence s where w.val ='" + word + "' and sw.word_id=w.id and s.id=sw.sentence_id and sw.position=s.words), (select  count(*) as totF from word w, sentence_word sw, sentence s where w.val ='" + word + "' and sw.word_id=w.id and s.id=sw.sentence_id)";
            OracleCallableStatement stmt = (OracleCallableStatement) dbConnection.prepareCall(sql);
            ResultSet rst = stmt.executeQuery();
            rst.next();
            double prob =0;
            double endF = rst.getDouble(1);
            double totF = rst.getDouble(2);

            if(totF>0){
                prob = endF/totF;
            }
            rst.close();
            stmt.close();
            return prob;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }


}
