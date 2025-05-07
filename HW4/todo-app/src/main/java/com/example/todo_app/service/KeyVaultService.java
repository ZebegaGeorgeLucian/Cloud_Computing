package com.example.todo_app.service;

import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.stereotype.Service;

@Service
public class KeyVaultService {
    private final SecretClient secretClient;

    public KeyVaultService() {
        this.secretClient = new SecretClientBuilder()
                .vaultUrl("https://todo-keyvault1.vault.azure.net/")
                .credential(new AzureCliCredentialBuilder().build()) // Use Azure CLI authentication
                .buildClient();
    }

    public String getSecret(String secretName) {
        return secretClient.getSecret(secretName).getValue();
    }
}