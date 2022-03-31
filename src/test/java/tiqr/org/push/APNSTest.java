package tiqr.org.push;

import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.server.*;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class APNSTest {

    @Test
    void push() throws IOException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException, ExecutionException {

        MockApnsServer server = buildServer(new AcceptAllPushNotificationHandlerFactory(), null);
        server.start(8099).get();
        APNS apns = new APNS(
                "localhost",
                8099,
                new ClassPathResource("token-auth-private-key.p8"),
                Optional.of(new ClassPathResource("/ca.pem")),
                "teamId", "keyId");
        PushNotificationResponse<SimpleApnsPushNotification> response = apns.push("123456789").get();

        assertTrue(response.isAccepted());
        assertEquals(200, response.getStatusCode());
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