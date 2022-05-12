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
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class NotificationGatewayTest {

    @RegisterExtension
    WireMockExtension mockServer = new WireMockExtension(8999);

    private MockApnsServer server;
    private static NotificationGateway notificationGateway;

    @BeforeAll
    static void beforeAll() {
        notificationGateway = new NotificationGateway(
                new APNSConfiguration(
                        "localhost",
                        8099,
                        "classpath:token-auth-private-key.p8",
                        "classpath:/ca.pem",
                        "topic",
                        "teamId",
                        "keyId"),
                new GCMConfiguration(
                        "classpath:/test-firebase-adminsdk.json",
                        "tiqr-java-connector")
        );
    }

    @BeforeEach
    void beforeEach() throws Exception {
        server = buildServer(new AcceptAllPushNotificationHandlerFactory(), null);
        server.start(8099).get();
    }

    @AfterEach
    void afterEach() throws ExecutionException, InterruptedException {
        server.shutdown().get();
    }

    @Test
    void pushAPNS() {
        Registration registration = getRegistration("APNS");
        String uuid = notificationGateway.push(registration, "https://eduid.nl/tiqrauth");
        assertNotNull(uuid);
    }

    @Test
    void pushAPNSException() {
        server.shutdown();
        Registration registration = getRegistration("APNS");
        assertThrows(PushNotificationException.class, () -> notificationGateway.push(registration, "https://eduid.nl/tiqrauth"));
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
        String uuid = notificationGateway.push(registration, "https://eduid.nl/tiqrauth");
        assertEquals("test", uuid);

        stubFor(post(urlPathMatching("/message")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(401)));
        assertThrows(PushNotificationException.class, () -> notificationGateway.push(registration, "https://eduid.nl/tiqrauth"));
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