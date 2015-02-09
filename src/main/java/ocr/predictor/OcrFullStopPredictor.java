package ocr.predictor;

import ocr.predictor.db.DBManager;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lahiru
 */
public class OcrFullStopPredictor {

    DBManager dbManager;
    
    /* ඒ. is not here. ******************************************/
    private final String shortForms[] = {"බී", "සී", "ඩී", "ඊ", "එෆ්", "ජී", "එච්",
                                        "අයි", "ජේ", "කේ", "එල්", "එම්", "එන්", "ඕ",
                                        "පී", "කිව්", "ආර්", "එස්", "ටී", "යූ", "ඩබ්", "ඩබ්ලිව්",
                                        "එක්ස්", "වයි", "ඉසෙඩ්",
                                        "පෙ", "ව", "ප",
                                        "රු",
                                        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
                                       };

    private String detectFullStops(String str) {
        for(String shortForm : shortForms) {
            if(str.equals(shortForm)) {
                return str + ".";
            }
        }
        
        double prob = dbManager.getFinalWordProb(str);
        System.out.println(str + " : " + prob);
        if (prob > 0.8) {
            str = str + ".";
        }
        return str;
    }


    private void writeToOutputFile(String path, String str) {
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
            try {
                LinkedList<String> lines = new LinkedList<>();
                InputStream originalIS = OcrFullStopPredictor.class.getClassLoader().getResourceAsStream(
                        "dotReplaced/" + counter + ".txt");
                BufferedReader originalBR = new BufferedReader(new InputStreamReader(originalIS));
                String line;
                while((line = originalBR.readLine()) != null) {
                    lines.addLast(line);
                }
                
                String writeLocation = "/home/" + System.getProperty("user.name") + "/final/" + counter + ".txt";
                for (int i = 0; i < lines.size(); i++) {
                    SinhalaTokenizerIgnoringDots tokenizer = new SinhalaTokenizerIgnoringDots();

                    List<String> wordList = tokenizer.splitWords(lines.get(i));
                    for (int j = 0; j < wordList.size(); j++) {
                        String output = detectFullStops(wordList.get(j));
                        output = output + " ";
                        writeToOutputFile(writeLocation, output);
                    }
                    writeToOutputFile(writeLocation, "\n");
                }
                originalBR.close();
                originalIS.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new OcrFullStopPredictor();
    }

    //select (endF*1.0/totF) from (select  count(*) as endF from word w, sentence_word sw, sentence s where w.val ='බවද' and sw.word_id=w.id and s.id=sw.sentence_id and sw.position=s.words), (select  count(*) as totF from word w, sentence_word sw, sentence s where w.val ='බවද' and sw.word_id=w.id and s.id=sw.sentence_id)
}
