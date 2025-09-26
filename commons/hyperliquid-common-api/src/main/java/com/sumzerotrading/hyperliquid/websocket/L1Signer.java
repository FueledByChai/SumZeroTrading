package com.sumzerotrading.hyperliquid.websocket;

import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.StructuredDataEncoder;
import org.web3j.utils.Numeric;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class L1Signer {
    private final ECKeyPair keyPair;
    private final boolean isMainnet;
    private static final ObjectMapper MP = new ObjectMapper(new MessagePackFactory());
    private static final ObjectMapper JSON = new ObjectMapper();

    public static final class Sig {
        public final String r, s;
        public final int v;

        public Sig(String r, String s, int v) {
            this.r = r;
            this.s = s;
            this.v = v;
        }
    }

    public L1Signer(ECKeyPair keyPair, boolean isMainnet) {
        this.keyPair = keyPair;
        this.isMainnet = isMainnet;
    }

    /** action = your existing ActionPayload POJO (type/orders/grouping/builder) */
    public Sig sign(Object action, long nonceMs, String vaultAddressOrNull, Long expiresAfterMsOrNull)
            throws Exception {
        byte[] hash = actionHash(action, nonceMs, vaultAddressOrNull, expiresAfterMsOrNull);
        return signAgent(hash);
    }

    private Sig signAgent(byte[] actionHash) throws Exception {
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
        agent.add(obj("source", "string"));
        agent.add(obj("connectionId", "bytes32"));
        types.set("Agent", agent);
        ArrayNode eip712 = JSON.createArrayNode();
        eip712.add(obj("name", "string"));
        eip712.add(obj("version", "string"));
        eip712.add(obj("chainId", "uint256"));
        eip712.add(obj("verifyingContract", "address"));
        types.set("EIP712Domain", eip712);

        ObjectNode data = JSON.createObjectNode();
        data.set("types", types);
        data.set("domain", domain);
        data.put("primaryType", "Agent");
        data.set("message", message);

        StructuredDataEncoder enc = new StructuredDataEncoder(JSON.writeValueAsString(data));
        byte[] digest = enc.hashStructuredData();

        Sign.SignatureData sd = Sign.signMessage(digest, keyPair, false);
        String r = "0x" + Numeric.toHexStringNoPrefix(sd.getR());
        String s = "0x" + Numeric.toHexStringNoPrefix(sd.getS());
        int v = sd.getV()[0];
        if (v == 0 || v == 1)
            v += 27;
        return new Sig(r, s, v);
    }

    private static ObjectNode obj(String name, String type) {
        ObjectNode n = new ObjectMapper().createObjectNode();
        n.put("name", name);
        n.put("type", type);
        return n;
    }

    /**
     * Exactly matches SDK action_hash: msgpack(action) || nonce(u64 BE) ||
     * vaultFlag+addr || (0x00||expiresAfter u64 BE)
     */
    private static byte[] actionHash(Object action, long nonceMs, String vaultAddrOrNull, Long expiresAfterOrNull)
            throws Exception {
        byte[] packed = MP.writeValueAsBytes(action);

        byte[] nonce = new byte[8];
        long n = nonceMs;
        for (int i = 7; i >= 0; --i) {
            nonce[i] = (byte) (n & 0xFF);
            n >>>= 8;
        }

        byte[] vaultPart;
        if (vaultAddrOrNull == null) {
            vaultPart = new byte[] { 0x00 };
        } else {
            byte[] addr = Numeric.hexStringToByteArray(vaultAddrOrNull.toLowerCase());
            if (addr.length != 20)
                throw new IllegalArgumentException("vaultAddress must be 20 bytes");
            vaultPart = new byte[1 + 20];
            vaultPart[0] = 0x01;
            System.arraycopy(addr, 0, vaultPart, 1, 20);
        }

        byte[] expPart = new byte[0];
        if (expiresAfterOrNull != null) {
            byte[] exp = new byte[8];
            long e = expiresAfterOrNull;
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

        return org.web3j.crypto.Hash.sha3(data);
    }
}
