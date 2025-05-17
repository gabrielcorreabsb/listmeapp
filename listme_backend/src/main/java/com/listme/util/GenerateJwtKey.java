package com.listme.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;

public class GenerateJwtKey {
    public static void main(String[] args) {
        // Gerar uma chave segura para HS512
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        // Converter a chave para Base64
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());

        // Imprimir a chave
        System.out.println("Chave JWT gerada (Base64):");
        System.out.println(encodedKey);

        // Imprimir o tamanho da chave em bits
        System.out.println("\nTamanho da chave: " + (key.getEncoded().length * 8) + " bits");
    }
}
