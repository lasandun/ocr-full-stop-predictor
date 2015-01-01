package ocr.predictor;

import corpus.sinhala.SinhalaTokenizer;
import ocr.predictor.db.DBManager;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author lahiru
 */
public class OcrFullStopPredictor {

    DBManager dbManager;

    public String detectFullStops(String str) {
        double prob = dbManager.getFinalWordProb(str);
        System.out.println(str + " : " + prob);
        if (prob > 0.8) {
            str = str + ".";
        }
        return str;
    }


    public void writeToOutputFile(String path, String str) {
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path, true), "utf-8"));
            writer.write(str);
        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
    }

    public OcrFullStopPredictor() {
        dbManager = new DBManager();

        for (int counter = 1; counter <= 20; counter++) {

            Path path = Paths.get("/Users/dimuthuupeksha/Documents/Academic/FYP/ocr-full-stop-predictor/src/test/resources/dotReplaced/"+counter+".txt");
            String writeLocation = "/Users/dimuthuupeksha/Documents/Academic/FYP/ocr-full-stop-predictor/src/test/resources/final/"+counter+".txt";
            try {
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    SinhalaTokenizer tokenizer = new SinhalaTokenizer();

                    List<String> wordList = tokenizer.splitWords(lines.get(i));
                    for (int j = 0; j < wordList.size(); j++) {
                        String output = detectFullStops(wordList.get(j));
                        output = output + " ";
                        writeToOutputFile(writeLocation, output);
                    }
                    writeToOutputFile(writeLocation, "\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //System.out.println("Hoooo");
    }

    public static void main(String[] args) {
        new OcrFullStopPredictor();
    }

    //select (endF*1.0/totF) from (select  count(*) as endF from word w, sentence_word sw, sentence s where w.val ='බවද' and sw.word_id=w.id and s.id=sw.sentence_id and sw.position=s.words), (select  count(*) as totF from word w, sentence_word sw, sentence s where w.val ='බවද' and sw.word_id=w.id and s.id=sw.sentence_id)
}
