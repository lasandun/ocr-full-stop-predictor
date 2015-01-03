package ocr.predictor;

/**
 *
 * @author lahiru
 */
public class ResultEvaluator {
    
    private String originalText;
    private String newText;
        
    boolean debug = true;
    private int fp, fn, tp, tn; // false positive, false negative, true positive, true negative
    private int originalTextIndex, newTextIndex;
    private boolean calculated;

    public ResultEvaluator(String originalText, String newText) {
        this.originalText = originalText;
        this.newText = newText;
        calculated = false;
    }
    
    private void calculate() {
        calculated = true;
        fp = fn = tp = tn = 0;
        originalTextIndex = 0;
        newTextIndex = 0;
        
        while(originalTextIndex < originalText.length() && newTextIndex < newText.length()) {
            if(originalText.charAt(originalTextIndex) == '.' && newText.charAt(newTextIndex) == '.') {
                tp++;
                originalTextIndex++;
                newTextIndex++;
                tn--;
            }
            else if(originalText.charAt(originalTextIndex) == '.' && newText.charAt(newTextIndex) != '.') {
                fn++;
                originalTextIndex++;
                tn--;
            }
            else if(originalText.charAt(originalTextIndex) != '.' && newText.charAt(newTextIndex) == '.') {
                fp++;
                newTextIndex++;
            }
            else if(originalText.charAt(originalTextIndex) != '.' && originalText.charAt(originalTextIndex) == newText.charAt(newTextIndex)) {
                tn++;
                originalTextIndex++;
                newTextIndex++;
            }
            else {
                if(debug) {
                    System.out.println("not matching : " + originalText.charAt(originalTextIndex) + ", " + newText.charAt(newTextIndex));
                    System.out.println("index : " + originalTextIndex);
                    System.exit(-11);
                }
            }
        }
        
        if(debug) {
            if(originalTextIndex < originalText.length() - 1 || newTextIndex < newText.length() - 1) {
                System.out.println("half scanned or multiple dots at the end.");
                System.out.println("index- original: " + originalTextIndex + "   converted: " + newTextIndex);
                System.exit(-11);
            }
        }
        
        if(originalTextIndex == originalText.length() - 1) {
            if(originalText.charAt(originalTextIndex + 1) == '.') {
                fn++;
                originalTextIndex++;
                tn--;
            }
        }
        if(newTextIndex == newText.length() - 1) {
            if(newText.charAt(newTextIndex + 1) == '.') {
                fp++;
                newTextIndex++;
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
    public static void main(String[] args) {
        
        String originalText = "aab.dde.tt.d.dds.ss.a.ax.";
        String newText      = "aab.ddettd.dds.s.s.aax.";
        ResultEvaluator x = new ResultEvaluator(originalText, newText);
        System.out.println("precision : " + x.getPrecision());
        System.out.println("recall : " + x.getRecall());
        System.out.println("accuracy : " + x.getAccuracy());
        
    }
    
}

