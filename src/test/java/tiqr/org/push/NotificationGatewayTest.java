package tiqr.org.push;

import com.eatthepath.pushy.apns.server.*;
import com.google.common.base.Supplier;
import com.google.firebase.messaging.FirebaseMessaging;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import tiqr.org.WireMockExtension;
import tiqr.org.model.Registration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class NotificationGatewayTest {

    @RegisterExtension
    WireMockExtension mockServer = new WireMockExtension(8999);

    private MockApnsServer server;
    private static NotificationGateway notificationGateway;

    @BeforeAll
    static void beforeAll() throws IOException, ExecutionException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException {
        notificationGateway = new NotificationGateway(
                "localhost",
                8099,
                new ClassPathResource("token-auth-private-key.p8"),
                Optional.of(new ClassPathResource("/ca.pem")),
                "teamId", "keyId",
                new ClassPathResource("test-firebase-adminsdk.json"),
                "tiqr-java-connector"
        );
    }

    @BeforeEach
    void beforeEach() throws IOException, ExecutionException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException {
        server = buildServer(new AcceptAllPushNotificationHandlerFactory(), null);
        server.start(8099).get();
    }

    @AfterEach
    void afterEach() {
        server.shutdown();
    }

    @Test
    void pushAPNS() {
        Registration registration = getRegistration("APNS");
        String uuid = notificationGateway.push(registration);
        assertNotNull(uuid);
    }

    @Test
    void pushAPNSException() {
        server.shutdown();
        Registration registration = getRegistration("APNS");
        assertThrows(PushNotificationException.class, () -> notificationGateway.push(registration));
    }

    @Test
    void pushGCM() {
        Registration registration = getRegistration("GCM");

        tweakFCMSendUrl();

        String tokenResponse = "{\"expires_in\":500,\"access_token\":\"test\"}";
        stubFor(post(urlPathMatching("/token")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(tokenResponse)));

        String messageResponse = "{\"name\":\"test\"}";
        stubFor(post(urlPathMatching("/message")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(messageResponse)));
        String uuid = notificationGateway.push(registration);
        assertEquals("test", uuid);

        stubFor(post(urlPathMatching("/message")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(401)));
        assertThrows(PushNotificationException.class, () -> notificationGateway.push(registration));
    }

    private Registration getRegistration(String notificationType) {
        Registration registration = new Registration();
        registration.setUserId("userId");
        registration.setNotificationAddress("123456789");
        registration.setNotificationType(notificationType);
        return registration;
    }

    private void tweakFCMSendUrl() {
        GCM gcm = (GCM) ReflectionTestUtils.getField(this.notificationGateway, "gcm");
        FirebaseMessaging firebaseMessaging = (FirebaseMessaging) ReflectionTestUtils.getField(gcm, "firebaseMessaging");
        Supplier supplier = (Supplier) ReflectionTestUtils.getField(firebaseMessaging, "messagingClient");
        Object firebaseMessagingClient = supplier.get();
        ReflectionTestUtils.setField(firebaseMessagingClient, "fcmSendUrl", "http://localhost:8999/message");
    }

    private MockApnsServer buildServer(final PushNotificationHandlerFactory handlerFactory, final MockApnsServerListener listener) throws IOException {
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