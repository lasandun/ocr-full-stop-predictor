package ocr.predictor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lahiru
 */
public class ResultEvaluator {
    
    private String originalText;
    private String finalText;
        
    private final boolean debug = true;
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
    
    public void evaluate() throws IOException {
        String testDir = "/home/" + System.getProperty("user.name")+ "/Desktop/resources/";
        
        for(int counter = 1; counter <= 1; ++counter) {
            originalText = "";
            finalText = "";
            calculated = false;
            try {
                String line;
                BufferedReader br = new BufferedReader(new FileReader(testDir + "original/" + counter + ".txt"));
                while( (line = br.readLine()) != null) {
                    originalText += line;
                }
                br = new BufferedReader(new FileReader(testDir + "final/" + counter + ".txt"));
                while((line = br.readLine()) != null) {
                    finalText += line;
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ResultEvaluator.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            
            System.out.println("original: " + originalText);
            System.out.println("final: " + finalText);
            
            calculate();
            
            System.out.println("precision : " + getPrecision());
            System.out.println("recall : " + getRecall());
            System.out.println("accuracy : " + getAccuracy());
        }
    }
    
    private void calculate() {
        calculated = true;
        fp = fn = tp = tn = 0;
        originalTextIndex = 0;
        finalTextIndex = 0;
        
        while(originalTextIndex < originalText.length() && finalTextIndex < finalText.length()) {
            System.out.println(originalText.charAt(originalTextIndex) + "  " + finalText.charAt(finalTextIndex));
            System.out.println(originalTextIndex + "  " + finalTextIndex);
            if(originalText.charAt(originalTextIndex) == '.' && finalText.charAt(finalTextIndex) == '.') {
                tp++;
                originalTextIndex++;
                finalTextIndex++;
                tn--;
            }
            else if(originalText.charAt(originalTextIndex) == '.' && finalText.charAt(finalTextIndex) != '.') {
                fn++;
                originalTextIndex++;
                tn--;
            }
            else if(originalText.charAt(originalTextIndex) != '.' && finalText.charAt(finalTextIndex) == '.') {
                fp++;
                finalTextIndex++;
            }
            else if(originalText.charAt(originalTextIndex) != '.' && originalText.charAt(originalTextIndex) == finalText.charAt(finalTextIndex)) {
                tn++;
                originalTextIndex++;
                finalTextIndex++;
            }
            else {
                if(debug) {
                    System.out.println("not matching : " + originalText.charAt(originalTextIndex) + ", " + finalText.charAt(finalTextIndex));
                    System.out.println("index : " + originalTextIndex);
                    System.exit(-11);
                }
            }
        }
        
        if(debug) {
            if(originalTextIndex < originalText.length() - 1 || finalTextIndex < finalText.length() - 1) {
                System.out.println("half scanned or multiple dots at the end.");
                System.out.println("index- original: " + originalTextIndex + "   converted: " + finalTextIndex);
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
            System.out.println("tp = " + tp);
            System.out.println("tn = " + tn);
            System.out.println("fp = " + fp);
            System.out.println("fn = " + fn);
        }
        
    }
    
    public double getPrecision() {
        if(!calculated) calculate();
        return (tp * 1.0) / (fp + fn);
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
        
//        String originalText = "aab.dde.tt.d.dds.ss.a.ax.";
//        String newText      = "aab.ddettd.dds.s.s.aax.";
//        ResultEvaluator x = new ResultEvaluator(originalText, newText);
//        System.out.println("precision : " + x.getPrecision());
//        System.out.println("recall : " + x.getRecall());
//        System.out.println("accuracy : " + x.getAccuracy());
        
    }
    
}

