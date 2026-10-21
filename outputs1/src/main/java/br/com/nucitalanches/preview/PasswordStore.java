package br.com.nucitalanches.preview;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Credenciais locais da prévia: somente salt e hash, sem guardar senhas. */
final class PasswordStore {
    private record Credential(byte[] salt, byte[] hash) { }
    private final Map<String, Credential> credentials = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    void register(String email, char[] password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        credentials.put(email, new Credential(salt, derive(password, salt)));
    }

    boolean matches(String email, char[] password) {
        Credential credential = credentials.get(email);
        byte[] salt = credential == null ? new byte[16] : credential.salt();
        byte[] candidate = derive(password, salt);
        try {
            return credential != null && MessageDigest.isEqual(credential.hash(), candidate);
        } finally {
            Arrays.fill(candidate, (byte) 0);
        }
    }

    private byte[] derive(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 210_000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Não foi possível preparar as credenciais.", exception);
        } finally {
            spec.clearPassword();
        }
    }
}
