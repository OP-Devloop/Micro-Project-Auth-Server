package se.iths.oscarp.microprojectauthserver;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class KeyGenMain {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        String privateKey = Base64.getEncoder()
                .encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder()
                .encodeToString(keyPair.getPublic().getEncoded());
        System.out.println("Private key att kopiera:");
        System.out.println(privateKey);
        System.out.println("Put this key in a Environment Variable named JWT_PRIVATE_KEY");
        System.out.println("Public key att kopiera:");
        System.out.println(publicKey);
        System.out.println("Put this key in a Environment Variable named JWT_PUBLIC_KEY");
    }
}
