package com.sumzerotrading.hyperliquid.ws.json;

import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.StructuredDataEncoder;
import org.web3j.utils.Numeric;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sumzerotrading.data.SumZeroException;

import ch.qos.logback.classic.Logger;

public final class HLSigner {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HLSigner.class);
    private final ECKeyPair keyPair;

    public HLSigner(String privateKeyHex) {
        logger.info("Initializing HLSigner with provided private key.");
        this.keyPair = ECKeyPair.create(Numeric.toBigInt(privateKeyHex));
        logger.info("HLSigner initialized.");
        String localSigner = "0x" + Keys.getAddress(keyPair.getPublicKey()).toLowerCase();

    }

    /** Sign a user-signed exchange action (orders/TP-SL/etc.) */
    public SignatureFields signUserAction(SignableExchangeOrderRequest signable) throws Exception {
        // Validate required fields & exclusivity of t.limit vs t.trigger
        signable.validate();

        byte[] preimage = Mappers.MSGPACK.writeValueAsBytes(signable);
        byte[] digest = Hash.sha3(preimage);
        Sign.SignatureData sd = Sign.signMessage(digest, keyPair, false);
        return toSig(sd);

    }

    public SignatureFields signL1OrderAction(Object actionPojo, // your ActionPayload POJO
                                                                // (type/orders/grouping/builder)
            long nonceMs, String vaultAddressOrNull, // "0x..." or null
            Long expiresAfterMsOrNull, // nullable
            boolean isMainnet // true -> source "a", false -> "b"
    ) {

        byte[] actionHash = computeActionHash(actionPojo, nonceMs, vaultAddressOrNull, expiresAfterMsOrNull);
        logger.info("Computed action hash");
        ObjectNode typed = buildAgentTypedData(actionHash, isMainnet);
        logger.info("Built typed data");
        try {
            StructuredDataEncoder enc = new StructuredDataEncoder(JSON.writeValueAsString(typed));
            logger.info("Created structured data encoder");
            byte[] digest = enc.hashStructuredData();
            logger.info("Computed digest");
            Sign.SignatureData sd = Sign.signMessage(digest, keyPair, false);
            logger.info("Signed message");
            SignatureFields sig = toSig(sd);
            logger.info("Converted to signature fields");
            return sig;
        } catch (Exception e) {
            throw new SumZeroException("Error signing L1 order action", e);
        }
    }

    private static final ObjectMapper MSGPACK = new ObjectMapper(new MessagePackFactory());
    private static final ObjectMapper JSON = new ObjectMapper();

    private static SignatureFields toSig(Sign.SignatureData sd) {
        SignatureFields sig = new SignatureFields();
        sig.r = "0x" + Numeric.toHexStringNoPrefix(sd.getR());
        sig.s = "0x" + Numeric.toHexStringNoPrefix(sd.getS());
        int v = sd.getV()[0];
        sig.v = (v == 0 || v == 1) ? (v + 27) : v;
        return sig;
    }

    /**
     * SDK’s action_hash: msgpack(action) || nonce(u64 BE) || vaultFlag+addr ||
     * (0x00||expiresAfter u64 BE)
     */
    private static byte[] computeActionHash(Object actionPojo, long nonceMs, String vault, Long expiresAfter) {
        try {
            byte[] packed = MSGPACK.writeValueAsBytes(actionPojo);

            byte[] nonce = new byte[8];
            long n = nonceMs;
            for (int i = 7; i >= 0; --i) {
                nonce[i] = (byte) (n & 0xFF);
                n >>>= 8;
            }

            byte[] vaultPart;
            if (vault == null) {
                vaultPart = new byte[] { 0x00 };
            } else {
                byte[] addr = Numeric.hexStringToByteArray(vault.toLowerCase());
                if (addr.length != 20)
                    throw new IllegalArgumentException("vaultAddress must be 20 bytes");
                vaultPart = new byte[1 + 20];
                vaultPart[0] = 0x01;
                System.arraycopy(addr, 0, vaultPart, 1, 20);
            }

            byte[] expPart = new byte[0];
            if (expiresAfter != null) {
                byte[] exp = new byte[8];
                long e = expiresAfter;
                for (int i = 7; i >= 0; --i) {
                    exp[i] = (byte) (e & 0xFF);
                    e >>>= 8;
                }
                expPart = new byte[1 + 8];
                expPart[0] = 0x00;
                System.arraycopy(exp, 0, expPart, 1, 8);
            }

            byte[] data = new byte[packed.length + 8 + vaultPart.length + expPart.length];
            int off = 0;
            System.arraycopy(packed, 0, data, off, packed.length);
            off += packed.length;
            System.arraycopy(nonce, 0, data, off, 8);
            off += 8;
            System.arraycopy(vaultPart, 0, data, off, vaultPart.length);
            off += vaultPart.length;
            if (expPart.length > 0)
                System.arraycopy(expPart, 0, data, off, expPart.length);

            return Hash.sha3(data);
        } catch (Exception e) {
            throw new SumZeroException("Error computing action hash", e);
        }
    }

    private static ObjectNode buildAgentTypedData(byte[] actionHash, boolean isMainnet) {
        ObjectNode message = JSON.createObjectNode();
        message.put("source", isMainnet ? "a" : "b");
        message.put("connectionId", "0x" + Numeric.toHexStringNoPrefix(actionHash));

        ObjectNode domain = JSON.createObjectNode();
        domain.put("name", "Exchange");
        domain.put("version", "1");
        domain.put("chainId", 1337);
        domain.put("verifyingContract", "0x0000000000000000000000000000000000000000");

        ObjectNode types = JSON.createObjectNode();
        ArrayNode agent = JSON.createArrayNode();
        agent.add(field("source", "string"));
        agent.add(field("connectionId", "bytes32"));
        types.set("Agent", agent);
        ArrayNode eip712 = JSON.createArrayNode();
        eip712.add(field("name", "string"));
        eip712.add(field("version", "string"));
        eip712.add(field("chainId", "uint256"));
        eip712.add(field("verifyingContract", "address"));
        types.set("EIP712Domain", eip712);

        ObjectNode data = JSON.createObjectNode();
        data.set("types", types);
        data.set("domain", domain);
        data.put("primaryType", "Agent");
        data.set("message", message);
        return data;
    }

    private static ObjectNode field(String name, String type) {
        ObjectNode n = new ObjectMapper().createObjectNode();
        n.put("name", name);
        n.put("type", type);
        return n;
    }
}
