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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 * Evaluate results of the full stop predictor
 * @author lahiru
 */
public class ResultEvaluator {
    
    private final static Logger logger = Logger.getLogger(ResultEvaluator.class);
    private String originalText;
    private String finalText;
        
    private final boolean debug = false;
    private int fp, fn, tp, tn; // false positive, false negative, true positive, true negative
    private int originalTextIndex, finalTextIndex;
    private boolean calculated;

    public ResultEvaluator(String originalText, String newText) {
        this.finalText = newText;
        this.originalText = originalText;
        calculated = false;
    }
    
    public ResultEvaluator() {
        calculated = false;
    }
    
    /**
     * evaluate the results
     * @throws IOException 
     */
    public void evaluate() throws IOException {
        
        for(int counter = 1; counter <= 20; ++counter) {
            logger.info("##################### " + counter + ".txt #####################");
            originalText = "";
            finalText = "";
            calculated = false;
            
            try {
                String line;
                InputStream fis = getClass().getClassLoader().getResourceAsStream("original/" + counter + ".txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                while( (line = br.readLine()) != null) {
                    originalText += line;
                }
                fis.close();
                br.close();
                SinhalaTokenizerIgnoringDots tokenizer = new SinhalaTokenizerIgnoringDots();
                String temp = originalText;
                originalText = "";
                LinkedList<String> wordList = tokenizer.splitWords(temp);
                for(String word : wordList) {
                    originalText += word + " ";
                }
                
                br = new BufferedReader(new FileReader(SysProperty.getProperty("resultsPath") + counter + ".txt"));
                while((line = br.readLine()) != null) {
                    finalText += line;
                }
                fis.close();
                br.close();
            } catch (FileNotFoundException ex) {
                logger.error(ex);
                continue;
            }
            
            if(debug) {
                logger.info("original: " + originalText);
                logger.info("final: " + finalText);
            }
            
            calculate();
            
            logger.info("precision : " + getPrecision());
            logger.info("recall : " + getRecall());
            logger.info("tp = " + tp);
            logger.info("tn = " + tn);
            logger.info("fp = " + fp);
            logger.info("fn = " + fn);
        }
    }
    
    // calculate the precision and recall
    private void calculate() {
        calculated = true;
        fp = fn = tp = tn = 0;
        originalTextIndex = 0;
        finalTextIndex = 0;
        
        while(originalTextIndex < originalText.length() && finalTextIndex < finalText.length()) {
            if(originalText.charAt(originalTextIndex) == '.' && finalText.charAt(finalTextIndex) == '.') {
                originalTextIndex++;
                finalTextIndex++;
                tp++;
            }
            else if(originalText.charAt(originalTextIndex) == '.' && finalText.charAt(finalTextIndex) != '.') {
                originalTextIndex++;
                fn++;
            }
            else if(originalText.charAt(originalTextIndex) != '.' && finalText.charAt(finalTextIndex) == '.') {
                finalTextIndex++;
                fp++;
            }
            else if(originalText.charAt(originalTextIndex) != '.' 
                    && originalText.charAt(originalTextIndex) == finalText.charAt(finalTextIndex)) {
                // Space is the termination of words
                // The predictor uses SinhalaWordTokenizer. Therefore all the symbols and other
                // word separators will be replaced by a space.
                // True negative -> words of both final text and original text end without a dot
                if(originalText.charAt(originalTextIndex) == ' ') {
                    tn++;
                }
                
                originalTextIndex++;
                finalTextIndex++;
            }
            else {
                if(debug) {
                    logger.info("not matching : " + originalText.charAt(originalTextIndex)
                            + ", " + finalText.charAt(finalTextIndex));
                    logger.info("index : " + originalTextIndex);
                    System.exit(-11);
                }
            }
        }
        
        if(debug) {
            if(originalTextIndex < originalText.length() - 1 || finalTextIndex < finalText.length() - 1) {
                logger.info("half scanned or multiple dots at the end.");
                logger.info("index- original: " + originalTextIndex + "   converted: " + finalTextIndex);
                System.exit(-11);
            }
        }
        
        if(originalTextIndex < originalText.length()) {
            if(originalText.charAt(originalTextIndex) == '.') {
                fn++;
                originalTextIndex++;
                tn--;
            }
        }
        if(finalTextIndex < finalText.length()) {
            if(finalText.charAt(finalTextIndex) == '.') {
                fp++;
                finalTextIndex++;
            }
        }
        
        if(debug) {
            logger.info("tp = " + tp);
            logger.info("tn = " + tn);
            logger.info("fp = " + fp);
            logger.info("fn = " + fn);
        }
        
    }
    
    public double getPrecision() {
        if(!calculated) calculate();
        return (tp * 1.0) / (tp + fp);
    }
    
    public double getRecall() {
        if(!calculated) calculate();
        return (tp * 1.0) / (tp + fn);
    }
    
    public double getAccuracy() {
        if(!calculated) calculate();
        return (tn * 1.0 + fp) / (tn + tp + fp + fn);
    }
    
    // example implementation
    public static void main(String[] args) throws IOException {
        ResultEvaluator x = new ResultEvaluator();
        x.evaluate();
    }
    
}

