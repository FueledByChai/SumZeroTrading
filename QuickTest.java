import java.math.BigDecimal;
import java.math.RoundingMode;

public class QuickTest {
    public static void main(String[] args) {
        System.out.println("=== Order Size Formatting Issue ===");
        
        // ZORA token scenario
        BigDecimal orderSize = new BigDecimal("500.0");
        BigDecimal increment1 = new BigDecimal("1");     // scale = 0 
        BigDecimal increment2 = new BigDecimal("1.0");   // scale = 1 (BAD!)
        
        System.out.println("Input order size: " + orderSize.toPlainString());
        System.out.println("Increment 1 (scale=" + increment1.scale() + "): " + increment1.toPlainString());
        System.out.println("Increment 2 (scale=" + increment2.scale() + "): " + increment2.toPlainString());
        System.out.println();
        
        // Old problematic approach
        String result1 = orderSize.setScale(increment1.scale(), RoundingMode.DOWN).toPlainString();
        String result2 = orderSize.setScale(increment2.scale(), RoundingMode.DOWN).toPlainString();
        
        System.out.println("OLD METHOD:");
        System.out.println("  Using increment1: " + result1 + " ✓ (correct - no decimal)");
        System.out.println("  Using increment2: " + result2 + " ✗ (wrong - has decimal)");
        System.out.println();
        
        // New approach using stripTrailingZeros
        String newResult1 = formatOrderSize(orderSize, increment1);
        String newResult2 = formatOrderSize(orderSize, increment2);
        
        System.out.println("NEW METHOD (with stripTrailingZeros):");
        System.out.println("  Using increment1: " + newResult1 + " ✓");
        System.out.println("  Using increment2: " + newResult2 + " ✓ (fixed!)");
    }
    
    private static String formatOrderSize(BigDecimal orderSize, BigDecimal orderSizeIncrement) {
        BigDecimal strippedIncrement = orderSizeIncrement.stripTrailingZeros();
        int targetScale = Math.max(0, strippedIncrement.scale());
        BigDecimal roundedSize = orderSize.setScale(targetScale, RoundingMode.DOWN);
        return roundedSize.stripTrailingZeros().toPlainString();
    }
}