package com.technova.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class SenhaHasher {
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_CHAVE = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private SenhaHasher() {
    }

    public static String hash(String senha) {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        byte[] chave = derivar(senha.toCharArray(), sal, ITERACOES);
        return "pbkdf2$" + ITERACOES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(chave);
    }

    public static boolean verificar(String senha, String armazenamento) {
        if (senha == null || armazenamento == null) {
            return false;
        }
        if (!armazenamento.startsWith("pbkdf2$")) {
            return MessageDigest.isEqual(
                    senha.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    armazenamento.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        try {
            String[] partes = armazenamento.split("\\$", -1);
            if (partes.length != 4) {
                return false;
            }
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] atual = derivar(senha.toCharArray(), sal, iteracoes);
            return MessageDigest.isEqual(esperado, atual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] senha, byte[] sal, int iteracoes) {
        try {
            PBEKeySpec especificacao = new PBEKeySpec(senha, sal, iteracoes, TAMANHO_CHAVE);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(especificacao).getEncoded();
            } finally {
                especificacao.clearPassword();
                java.util.Arrays.fill(senha, '\0');
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Nao foi possivel processar a senha.", e);
        }
    }
}
