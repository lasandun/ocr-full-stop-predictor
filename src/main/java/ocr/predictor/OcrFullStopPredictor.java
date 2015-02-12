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

package ocr.predictor;

import ocr.predictor.db.DBManager;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class OcrFullStopPredictor {

    private final static Logger logger = Logger.getLogger(OcrFullStopPredictor.class);
    private DBManager dbManager;
    
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
        logger.info(str + " : " + prob);
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
                logger.error(e);
            }
        }
    }

    // example implementation
    public static void main(String[] args) {
        new OcrFullStopPredictor();
    }

}
