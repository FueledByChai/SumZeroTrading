# 📚 How to Generate Broker Documentation

This guide shows you multiple ways to generate comprehensive broker capability documentation for the SumZero Trading library.

## 🚀 Quick Start - Generate Documentation Now

### Method 1: Maven Plugin (Easiest)

```bash
cd /Users/RobTerpilowski/Code/JavaProjects/SumZeroTrading

# Generate documentation using Maven
mvn compile exec:java -pl examples/CryptoExamples \
  -Dexec.mainClass="com.sumzerotrading.documentation.GenerateBrokerDocumentation" \
  -Dexec.args="docs/generated"
```

### Method 2: Direct Java Execution

```bash
cd /Users/RobTerpilowski/Code/JavaProjects/SumZeroTrading

# First compile the project
mvn clean compile -pl examples/CryptoExamples

# Then run the documentation generator
java -cp "examples/CryptoExamples/target/classes:examples/CryptoExamples/target/dependency/*" \
  com.sumzerotrading.documentation.GenerateBrokerDocumentation \
  docs/generated
```

### Method 3: IDE Execution

1. **Open your IDE** (IntelliJ IDEA, Eclipse, VS Code)
2. **Navigate to**: `examples/CryptoExamples/src/main/java/com/sumzerotrading/documentation/GenerateBrokerDocumentation.java`
3. **Right-click** → **Run Main Method**
4. **Optional**: Set program arguments to specify output directory: `docs/generated`

## 📋 What Gets Generated

After running the documentation generator, you'll get these files:

```
docs/generated/
├── README.md                           # Main index with overview
├── broker-comparison.md                # Side-by-side comparison table
├── method-capability-matrix.md         # Which methods each broker supports
└── hyperliquid-capabilities.md         # Individual broker documentation
```

## 📊 Sample Output

### Generated Files Preview:

**README.md** - Main index:

```markdown
# SumZero Trading - Broker Capabilities Documentation

## Available Documentation

- [Broker Comparison](broker-comparison.md)
- [Method Capability Matrix](method-capability-matrix.md)
- [Hyperliquid](hyperliquid-capabilities.md)

## Summary Statistics

- Total Brokers: 1
- Total Supported Methods: 15
```

**broker-comparison.md** - Feature comparison:

```markdown
| Feature         | Hyperliquid |
| --------------- | ----------- |
| Real-time Data  | ✅          |
| Historical Data | ❌          |
| Paper Trading   | ✅          |
```

**method-capability-matrix.md** - Method support:

```markdown
| Method           | Description                   | Hyperliquid |
| ---------------- | ----------------------------- | ----------- |
| `cancelOrder()`  | Cancel single order by ID     | ✅          |
| `cancelOrders()` | Cancel multiple orders by IDs | ❌          |
```

**hyperliquid-capabilities.md** - Detailed broker docs:

```markdown
# Hyperliquid Broker

**Version:** 0.2.0
**Description:** High-performance perpetual futures DEX

## Supported Order Types

- **MARKET**: Converted to aggressive limit orders
- **LIMIT**: Standard limit orders
- **STOP**: Stop market orders
- **STOP_LIMIT**: Stop limit orders

## Limitations & Restrictions

- Market orders converted to aggressive limit with 5% slippage protection
- Only perpetual futures contracts supported
- Order modification requires cancel and replace
```

## 🔧 Customizing Documentation Generation

### Change Output Directory

```bash
# Generate docs to custom location
mvn exec:java -pl examples/CryptoExamples \
  -Dexec.mainClass="com.sumzerotrading.documentation.GenerateBrokerDocumentation" \
  -Dexec.args="/path/to/custom/output"
```

### Adding New Brokers

To include new brokers in documentation:

1. **Implement BrokerCapabilities**:

   ```java
   public class MyBrokerCapabilities extends AbstractBrokerCapabilities {
       private static final MyBrokerCapabilities INSTANCE = new MyBrokerCapabilities();

       private MyBrokerCapabilities() {
           super(new Builder("MyBroker")
               .description("My broker description")
               .supportedMethods(/* list methods */)
           );
       }
   }
   ```

2. **Update GenerateBrokerDocumentation.java**:

   ```java
   private static List<BrokerCapabilities> getAllBrokerCapabilities() {
       List<BrokerCapabilities> brokers = new ArrayList<>();
       brokers.add(HyperliquidBrokerCapabilities.getInstance());
       brokers.add(MyBrokerCapabilities.getInstance()); // ← Add this
       return brokers;
   }
   ```

3. **Regenerate docs**:
   ```bash
   mvn exec:java -pl examples/CryptoExamples \
     -Dexec.mainClass="com.sumzerotrading.documentation.GenerateBrokerDocumentation"
   ```

## 🔄 Automated Documentation Generation

### During Build Process

The Maven configuration automatically generates docs during compilation:

```xml
<!-- In examples/CryptoExamples/pom.xml -->
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>generate-docs</id>
            <phase>compile</phase>  <!-- Runs during mvn compile -->
            <goals>
                <goal>java</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### CI/CD Pipeline Integration

For automated documentation updates in CI/CD:

```yaml
# .github/workflows/generate-docs.yml
name: Generate Documentation
on: [push, pull_request]

jobs:
  generate-docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: "21"
      - name: Generate Documentation
        run: |
          mvn compile exec:java -pl examples/CryptoExamples \
            -Dexec.mainClass="com.sumzerotrading.documentation.GenerateBrokerDocumentation" \
            -Dexec.args="docs/generated"
      - name: Commit Documentation
        run: |
          git config --local user.email "action@github.com"
          git config --local user.name "GitHub Action"
          git add docs/generated/
          git commit -m "Auto-update broker documentation" || exit 0
          git push
```

## 🐛 Troubleshooting

### Common Issues:

**"Class not found" error**:

```bash
# Make sure to compile first
mvn clean compile -pl examples/CryptoExamples
```

**"Permission denied" on output directory**:

```bash
# Create directory with proper permissions
mkdir -p docs/generated
chmod 755 docs/generated
```

**"No brokers found" message**:

- Check that broker capability classes are properly imported
- Verify the `getAllBrokerCapabilities()` method includes your brokers

### Debugging:

Add debug output to see what's happening:

```java
public static void main(String[] args) {
    System.setProperty("java.util.logging.ConsoleHandler.level", "ALL");
    // ... rest of main method
}
```

## 📖 Next Steps

1. **Run the generator** using one of the methods above
2. **Check the output** in `docs/generated/`
3. **Review the documentation** to ensure accuracy
4. **Add more brokers** as you implement their capabilities
5. **Integrate into build process** for automatic updates

The generated documentation will help users understand exactly what each broker supports and provide clear guidance on method-level capabilities!
