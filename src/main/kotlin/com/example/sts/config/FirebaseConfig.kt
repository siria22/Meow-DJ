package com.example.sts.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.core.io.ClassPathResource
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import java.io.ByteArrayInputStream
import java.io.InputStream

@Configuration
class FirebaseConfig {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Bean
    fun firebaseApp(environment: Environment): FirebaseApp { // Spring의 Environment 주입
        val isProdProfile = environment.acceptsProfiles(Profiles.of("prod"))
        val serviceAccountStream = if (isProdProfile) {
            logger.info("PROD profile active. Fetching Firebase key from AWS Secrets Manager.")
            val stream = getStreamFromSecretsManager()
            logger.info("==> Successfully fetched Firebase key from AWS Secrets Manager.")
            stream
        } else {
            logger.info("===================================================================")
            logger.info("==> LOCAL/DEV profile active. Fetching Firebase key from classpath.")
            logger.info("===================================================================")
            getStreamFromClasspath()
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
            .build()

        return if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        } else {
            FirebaseApp.getInstance()
        }
    }

    @Bean
    fun firebaseAuth(firebaseApp: FirebaseApp): com.google.firebase.auth.FirebaseAuth {
        return com.google.firebase.auth.FirebaseAuth.getInstance(firebaseApp)
    }

    private fun getStreamFromSecretsManager(): InputStream {
        val secretName = "prod/firebase/serviceAccountKey"
        val region = Region.AP_NORTHEAST_2
        val secretsClient = SecretsManagerClient.builder()
            .region(region)
            .build()

        return try {
            val secretJson = secretsClient.getSecretValue(GetSecretValueRequest.builder().secretId(secretName).build())
                .secretString()
            ByteArrayInputStream(secretJson.toByteArray())
        } catch (e: Exception) {
            logger.error("Failed to fetch secret from AWS Secrets Manager", e)
            throw RuntimeException("Failed to fetch secret from AWS Secrets Manager: ${e.message}", e)
        } finally {
            secretsClient.close()
        }
    }

    private fun getStreamFromClasspath(): InputStream {
        return try {
            ClassPathResource("firebaseServiceAccountKey.json").inputStream
        } catch (e: Exception) {
            logger.error("Failed to load Firebase key from classpath", e)
            throw RuntimeException("Failed to load Firebase key from classpath: ${e.message}", e)
        }
    }
}
