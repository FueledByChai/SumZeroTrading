import java.math.BigDecimal;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Exchange;

public class PriceFormattingTest {
    public static void main(String[] args) {
        // Create a ticker with minimum tick size 0.001 (3 decimal places)
        Ticker ticker = new Ticker();
        ticker.setSymbol("BTC-PERP");
        ticker.setInstrumentType(InstrumentType.PERPETUAL_FUTURES);
        ticker.setExchange(Exchange.INTERACTIVE_BROKERS_SMART);
        ticker.setMinimumTickSize(new BigDecimal("0.001"));
        
        // Test cases
        String[] testPrices = {"1.25", "123.4", "0.1", "45.678", "100"};
        
        System.out.println("Testing price formatting with minimum tick size: " + ticker.getMinimumTickSize());
        System.out.println("Original Price -> Formatted Price");
        System.out.println("-".repeat(40));
        
        for (String priceStr : testPrices) {
            BigDecimal original = new BigDecimal(priceStr);
            BigDecimal formatted = ticker.formatPrice(priceStr);
            System.out.printf("%-13s -> %s%n", priceStr, formatted);
        }
        
        System.out.println();
        
        // Test with a different minimum tick size (2 decimal places)
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        System.out.println("Testing with minimum tick size: " + ticker.getMinimumTickSize());
        System.out.println("Original Price -> Formatted Price");
        System.out.println("-".repeat(40));
        
        for (String priceStr : testPrices) {
            BigDecimal formatted = ticker.formatPrice(priceStr);
            System.out.printf("%-13s -> %s%n", priceStr, formatted);
        }
    }
}
