package tiqr.org.push;

import com.eatthepath.pushy.apns.server.*;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tiqr.org.model.Registration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class APNSTest {

    @Test
    void push() throws IOException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException, ExecutionException, PushNotificationException {
        MockApnsServer server = buildServer(new AcceptAllPushNotificationHandlerFactory(), null);
        server.start(8099).get();
        APNS apns = new APNS(new APNSConfiguration(
                "localhost",
                8099,
                "classpath:/token-auth-private-key.p8",
                "classpath:/ca.pem",
                "topic",
                "teamId",
                "keyId"));
        Registration registration = new Registration();
        registration.setUserId("userId");
        registration.setNotificationAddress("123456789");
        String uuid = apns.push(registration, "https://eduid.nl/tiqrauth");
        assertNotNull(uuid);
        server.shutdown().get();
    }

    protected MockApnsServer buildServer(final PushNotificationHandlerFactory handlerFactory, final MockApnsServerListener listener) throws IOException {
        return new MockApnsServerBuilder()
                .setServerCredentials(
                        new ClassPathResource("server-certs.pem").getInputStream(),
                        new ClassPathResource("server-key.pem").getInputStream(),
                        null)
                .setTrustedClientCertificateChain(new ClassPathResource("ca.pem").getInputStream())
                .setEventLoopGroup(new NioEventLoopGroup(2))
                .setHandlerFactory(handlerFactory)
                .setListener(listener)
                .build();
    }

}