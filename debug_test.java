import java.math.BigDecimal;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.HyperliquidUtil;

public class DebugTest {
    public static void main(String[] args) {
        // Recreate the failing test scenario
        Ticker t6 = new Ticker("TEST");
        t6.setMinimumTickSize(BigDecimal.ONE.divide(BigDecimal.TEN.pow(6))); // 0.000001
        
        BigDecimal price = new BigDecimal("1.25");
        
        System.out.println("Ticker minimumTickSize: " + t6.getMinimumTickSize());
        System.out.println("Ticker minimumTickSize scale: " + t6.getMinimumTickSize().scale());
        System.out.println("Price: " + price);
        System.out.println("Price scale: " + price.scale());
        System.out.println("Price stripTrailingZeros: " + price.stripTrailingZeros());
        System.out.println("Price stripTrailingZeros scale: " + price.stripTrailingZeros().scale());
        
        String result = HyperliquidUtil.formatPriceAsString(t6, price);
        System.out.println("Result: " + result);
        System.out.println("Expected: 1.2500");
    }
}