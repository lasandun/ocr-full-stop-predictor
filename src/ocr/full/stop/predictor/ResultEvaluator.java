package ocr.full.stop.predictor;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author lahiru
 */
public class ResultEvaluator {
    
    String originalText;
    String newText;
    LinkedList<String> newParts;
    LinkedList<String> originalParts;
    LinkedList<String> missedDots;
    LinkedList<String> unwantedDots;
    
    boolean debug = false;

    public ResultEvaluator(String originalText, String newText) {
        this.originalText = originalText;
        this.newText      = newText;
        newParts      = new LinkedList<String>();
        originalParts = new LinkedList<String>();
        missedDots = new LinkedList<String>();
        unwantedDots = new LinkedList<String>();
        
        String partsOfOriginalText[] = originalText.split("\\.");
        String partsOfNewText[]      = newText.split("\\.");
        
        for(String s : partsOfNewText) {
            newParts.addLast(s);
        }
        for(String s : partsOfOriginalText) {
            originalParts.addLast(s);
        }
    }
    
    // remove identical parts from both lists
    private void removeSuccessfullyIdentifiedParts() {
        boolean toNext = false;
        Iterator<String> originalIt = originalParts.iterator();
        
        while(originalIt.hasNext()) {
            String original = originalIt.next();
            Iterator<String> newTextIt = newParts.iterator();
            while(newTextIt.hasNext()) {
                String val = newTextIt.next();
                if(val.equals(original)) {
                    newTextIt.remove();
                    originalIt.remove();
                    toNext = true;
                    break;
                }
            }
            if(toNext) {
                toNext = false;
                continue;
            }
        }        
    }
    
    private void identifyMissedDots() {
        Iterator<String> originalIt = originalParts.iterator();
        String s1, s2;
        if(originalIt.hasNext()) {
            s1 = originalIt.next();
        } else {
            return;
        }
        
        while(originalIt.hasNext()) {
            s2 = originalIt.next();
            String connected = s1 + s2;
            
            Iterator<String> newIt = newParts.iterator();
            while(newIt.hasNext()) {
                String newTxt = newIt.next();
                if(newTxt.equals(connected)) {
                    String str = s1 + " + " + s2 + " = " + newTxt;
                    if(debug) System.out.println(str);
                    missedDots.addLast(str);
                    newIt.remove();
                    originalParts.removeFirstOccurrence(s1);
                    originalParts.removeFirstOccurrence(s2);
                    break;
                }
            }
            
            s1 = s2;
        }
    }
    
    private void identifyUnwantedDots() {
        Iterator<String> newIt = newParts.iterator();
        String s1, s2;
        if(newIt.hasNext()) {
            s1 = newIt.next();
        } else {
            return;
        }
        
        while(newIt.hasNext()) {
            s2 = newIt.next();
            String connected = s1 + s2;
            
            Iterator<String> originalIT = originalParts.iterator();
            while(originalIT.hasNext()) {
                String originalTxt = originalIT.next();
                if(originalTxt.equals(connected)) {
                    String str = originalTxt + " = " + s1 + " + " + s2;
                    unwantedDots.addLast(str);
                    if(debug) System.out.println(str);
                    originalIT.remove();
                    newParts.removeFirstOccurrence(s1);
                    newParts.removeFirstOccurrence(s2);
                    break;
                }
            }
            
            s1 = s2;
        }
    }
    
    public void processText() {
        removeSuccessfullyIdentifiedParts();
        identifyMissedDots();
        identifyUnwantedDots();
        showResults();
    }
    
    private void showResults() {
        System.out.println("########### summary ############");
        System.out.println("Original text--------:");
        for(String s : originalParts) {
            System.out.println(s);
        }
        System.out.println();
        
        System.out.println("new text-------------:");
        for(String s : newParts) {
            System.out.println(s);
        }
        System.out.println();
        
        System.out.println("missed dots----------:");
        for(String s : missedDots) {
            System.out.println(s);
        }
        System.out.println();
        
        System.out.println("unwanted dots--------:");
        for(String s : unwantedDots) {
            System.out.println(s);
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        String originalText = "aab.dde.ttd.dds.ss.aax.";
        String newText      = "aab.ddettd.dds.s.s.aax.";
        ResultEvaluator x = new ResultEvaluator(originalText, newText);
        x.processText();
    }
    
}
