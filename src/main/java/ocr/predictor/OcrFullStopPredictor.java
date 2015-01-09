package ocr.predictor;

import corpus.sinhala.SinhalaTokenizer;
import ocr.predictor.db.DBManager;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author lahiru
 */
public class OcrFullStopPredictor {

    DBManager dbManager;
    
    private final String shortForms[] = {"ඒ", "බී", "සී", "ඩී", "ඊ", "එෆ්", "ජී", "එච්",
                                        "අයි", "ජේ", "කේ", "එල්", "එම්", "එන්", "ඕ",
                                        "පී", "කිව්", "ආර්", "එස්", "ටී", "යූ", "ඩබ්", "ඩබ්ලිව්",
                                        "එක්ස්", "වයි", "ඉසෙඩ්",
                                        "පෙ", "ව", "ප",
                                        "රු",
                                        "පා", // parliment
                                        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
                                       };

    public String detectFullStops(String str) {
        for(String shortForm : shortForms) {
            if(str.equals(shortForm)) {
                str = str + ".";
            }
        }
        
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
            
            String testDir = "/home/maduranga/temp/ocr-full-stop-predictor/src/test/resources/";
            Path path = Paths.get(testDir + "dotReplaced/" + counter + ".txt");
            String writeLocation = testDir + "final/" + counter + ".txt";
            
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
