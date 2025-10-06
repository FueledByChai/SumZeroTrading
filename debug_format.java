import java.math.BigDecimal;
import com.sumzerotrading.data.Ticker;

public class DebugFormat {
    public static void main(String[] args) {
        // Test case: makeTickerWithSzDecimals(0) with input 1.23456789
        int maxDecimals = 6;
        int szDecimalsParam = 0;
        
        Ticker t = new Ticker("TEST");
        BigDecimal tickSize = BigDecimal.ONE.divide(BigDecimal.TEN.pow(maxDecimals - szDecimalsParam));
        t.setMinimumTickSize(tickSize);
        
        BigDecimal price = new BigDecimal("1.23456789");
        
        System.out.println("szDecimalsParam: " + szDecimalsParam);
        System.out.println("Tick size: " + tickSize);
        System.out.println("Tick size scale (szDecimals): " + tickSize.scale());
        System.out.println("Price: " + price);
        System.out.println("Price sig figs: " + countSignificantFigures(price.toPlainString()));
        
        int szDecimals = tickSize.scale();
        int allowedDecimalPlaces = Math.max(0, maxDecimals - szDecimals);
        System.out.println("Allowed decimal places: " + allowedDecimalPlaces);
        System.out.println("Expected result: 1.2345");
    }
    
    private static int countSignificantFigures(String s) {
        String digits = s.replaceFirst("^-?0*", "");
        if (digits.contains(".")) {
            digits = digits.replaceFirst("\\.", "");
            digits = digits.replaceFirst("0+$", "");
        }
        digits = digits.replace("-", "");
        return digits.length();
    }
}