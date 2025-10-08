/**
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.sumzerotrading.documentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.sumzerotrading.broker.capabilities.BrokerCapabilities;
import com.sumzerotrading.broker.documentation.BrokerCapabilityDocumentationGenerator;
import com.sumzerotrading.broker.hyperliquid.capabilities.HyperliquidBrokerCapabilities;

/**
 * Main class for generating comprehensive broker capability documentation. This
 * can be run as a standalone application to generate markdown documentation.
 */
public class GenerateBrokerDocumentation {

    private static final String DEFAULT_OUTPUT_DIR = "docs/generated";

    public static void main(String[] args) {
        String outputDir = args.length > 0 ? args[0] : DEFAULT_OUTPUT_DIR;

        try {
            System.out.println("🚀 Generating SumZero Trading Broker Documentation...");
            System.out.println("📁 Output directory: " + outputDir);

            // Create output directory
            Path outputPath = Paths.get(outputDir);
            Files.createDirectories(outputPath);

            // Collect all broker capabilities
            List<BrokerCapabilities> allBrokers = getAllBrokerCapabilities();

            // Generate individual broker documentation
            System.out.println("\n📋 Generating individual broker documentation:");
            for (BrokerCapabilities broker : allBrokers) {
                generateIndividualBrokerDoc(broker, outputDir);
            }

            // Generate comparison documentation
            generateComparisonDoc(allBrokers, outputDir);

            // Generate method capability matrix
            generateMethodCapabilityMatrix(allBrokers, outputDir);

            // Generate README index
            generateReadmeIndex(allBrokers, outputDir);

            System.out.println("\n✅ Documentation generation completed successfully!");
            System.out.println("📖 Check the following files:");
            System.out.println("   - " + outputDir + "/README.md (main index)");
            System.out.println("   - " + outputDir + "/broker-comparison.md (comparison table)");
            System.out.println("   - " + outputDir + "/method-capability-matrix.md (method support)");
            for (BrokerCapabilities broker : allBrokers) {
                System.out
                        .println("   - " + outputDir + "/" + broker.getBrokerName().toLowerCase() + "-capabilities.md");
            }

        } catch (Exception e) {
            System.err.println("❌ Error generating documentation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Get all available broker capabilities. Add new brokers here as they're
     * implemented.
     */
    private static List<BrokerCapabilities> getAllBrokerCapabilities() {
        List<BrokerCapabilities> brokers = new ArrayList<>();

        // Add Hyperliquid
        brokers.add(HyperliquidBrokerCapabilities.getInstance());

        // TODO: Add other brokers as they implement capabilities
        // brokers.add(InteractiveBrokersBrokerCapabilities.getInstance());
        // brokers.add(PaperBrokerCapabilities.getInstance());
        // brokers.add(ParadexBrokerCapabilities.getInstance());
        // brokers.add(DydxBrokerCapabilities.getInstance());

        return brokers;
    }

    /**
     * Generate documentation for a single broker
     */
    private static void generateIndividualBrokerDoc(BrokerCapabilities broker, String outputDir) throws IOException {
        System.out.println("   📝 Generating " + broker.getBrokerName() + " documentation...");

        String content = BrokerCapabilityDocumentationGenerator.generateBrokerDocumentation(broker);

        String fileName = broker.getBrokerName().toLowerCase().replace(" ", "-") + "-capabilities.md";
        Path outputFile = Paths.get(outputDir, fileName);

        Files.write(outputFile, content.getBytes());
        System.out.println("      ✅ " + fileName);
    }

    /**
     * Generate broker comparison documentation
     */
    private static void generateComparisonDoc(List<BrokerCapabilities> brokers, String outputDir) throws IOException {
        System.out.println("\n🔄 Generating broker comparison documentation...");

        String content = BrokerCapabilityDocumentationGenerator.generateBrokerComparison(brokers);

        Path outputFile = Paths.get(outputDir, "broker-comparison.md");
        Files.write(outputFile, content.getBytes());
        System.out.println("   ✅ broker-comparison.md");
    }

    /**
     * Generate method capability matrix showing which brokers support which methods
     */
    private static void generateMethodCapabilityMatrix(List<BrokerCapabilities> brokers, String outputDir)
            throws IOException {
        System.out.println("\n🔧 Generating method capability matrix...");

        StringBuilder doc = new StringBuilder();
        doc.append("# Method Capability Matrix\n\n");
        doc.append("This table shows which specific methods are supported by each broker implementation.\n\n");

        if (brokers.isEmpty()) {
            doc.append("No brokers available for comparison.\n");
        } else {
            // Header
            doc.append("| Method | Description |");
            for (BrokerCapabilities broker : brokers) {
                doc.append(" ").append(broker.getBrokerName()).append(" |");
            }
            doc.append("\n|--------|-------------|");
            for (int i = 0; i < brokers.size(); i++) {
                doc.append("----------|");
            }
            doc.append("\n");

            // Get all methods from all brokers
            java.util.Set<com.sumzerotrading.broker.capabilities.BrokerMethodCapability> allMethods = java.util.EnumSet
                    .noneOf(com.sumzerotrading.broker.capabilities.BrokerMethodCapability.class);
            for (BrokerCapabilities broker : brokers) {
                allMethods.addAll(broker.getSupportedMethods());
            }

            // Add rows for each method
            for (com.sumzerotrading.broker.capabilities.BrokerMethodCapability method : allMethods) {
                doc.append("| `").append(method.getMethodName()).append("()` | ").append(method.getDescription())
                        .append(" |");

                for (BrokerCapabilities broker : brokers) {
                    String support = broker.supportsMethod(method) ? "✅" : "❌";
                    doc.append(" ").append(support).append(" |");
                }
                doc.append("\n");
            }
        }

        Path outputFile = Paths.get(outputDir, "method-capability-matrix.md");
        Files.write(outputFile, doc.toString().getBytes());
        System.out.println("   ✅ method-capability-matrix.md");
    }

    /**
     * Generate main README index file
     */
    private static void generateReadmeIndex(List<BrokerCapabilities> brokers, String outputDir) throws IOException {
        System.out.println("\n📖 Generating README index...");

        StringBuilder doc = new StringBuilder();
        doc.append("# SumZero Trading - Broker Capabilities Documentation\n\n");
        doc.append("This directory contains comprehensive documentation of broker capabilities, ");
        doc.append("supported features, and API method support for all broker implementations ");
        doc.append("in the SumZero Trading library.\n\n");

        doc.append("## 📋 Available Documentation\n\n");
        doc.append("### Overview Documents\n");
        doc.append("- **[Broker Comparison](broker-comparison.md)** - Side-by-side feature comparison\n");
        doc.append(
                "- **[Method Capability Matrix](method-capability-matrix.md)** - Which methods each broker supports\n\n");

        doc.append("### Individual Broker Documentation\n");
        if (brokers.isEmpty()) {
            doc.append("*No broker documentation available yet.*\n");
        } else {
            for (BrokerCapabilities broker : brokers) {
                String fileName = broker.getBrokerName().toLowerCase().replace(" ", "-") + "-capabilities.md";
                doc.append("- **[").append(broker.getBrokerName()).append("](").append(fileName).append(")** - ")
                        .append(broker.getDescription()).append("\n");
            }
        }

        doc.append("\n## 🚀 Quick Start\n\n");
        doc.append("```java\n");
        doc.append("// Check if a broker supports a specific method\n");
        doc.append("HyperliquidBrokerCapabilities caps = HyperliquidBrokerCapabilities.getInstance();\n");
        doc.append("if (caps.supportsMethod(BrokerMethodCapability.CANCEL_ORDERS_BY_IDS)) {\n");
        doc.append("    broker.cancelOrders(orderIds);\n");
        doc.append("} else {\n");
        doc.append("    // Use individual cancellation fallback\n");
        doc.append("    for (String id : orderIds) {\n");
        doc.append("        broker.cancelOrder(id);\n");
        doc.append("    }\n");
        doc.append("}\n");
        doc.append("```\n\n");

        doc.append("## 📊 Summary Statistics\n\n");
        doc.append("- **Total Brokers**: ").append(brokers.size()).append("\n");
        if (!brokers.isEmpty()) {
            int totalMethods = brokers.stream().mapToInt(b -> b.getSupportedMethods().size()).sum();
            doc.append("- **Total Supported Methods**: ").append(totalMethods).append("\n");

            double avgMethods = brokers.stream().mapToInt(b -> b.getSupportedMethods().size()).average().orElse(0.0);
            doc.append("- **Average Methods per Broker**: ").append(String.format("%.1f", avgMethods)).append("\n");
        }

        doc.append("\n## 🔄 Regenerating Documentation\n\n");
        doc.append("To regenerate this documentation:\n\n");
        doc.append("```bash\n");
        doc.append("# Using Maven\n");
        doc.append("mvn exec:java -pl examples/CryptoExamples \\\n");
        doc.append("  -Dexec.mainClass=\"com.sumzerotrading.documentation.GenerateBrokerDocumentation\" \\\n");
        doc.append("  -Dexec.args=\"docs/generated\"\n\n");
        doc.append("# Or compile and run directly\n");
        doc.append("mvn compile\n");
        doc.append("java -cp \"target/classes:target/dependency/*\" \\\n");
        doc.append("  com.sumzerotrading.documentation.GenerateBrokerDocumentation docs/generated\n");
        doc.append("```\n\n");

        doc.append("---\n");
        doc.append("*Generated on: ").append(java.time.LocalDateTime.now().toString()).append("*\n");

        Path outputFile = Paths.get(outputDir, "README.md");
        Files.write(outputFile, doc.toString().getBytes());
        System.out.println("   ✅ README.md");
    }
}