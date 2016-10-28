package group.artifact;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLException;

import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ApnsClientBuilder;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;

import io.netty.util.concurrent.Future;

/**
 * You need to add following VM-arguments in your IDE before:
 * -Xbootclasspath/p:c:/alpn/alpn-boot-7.1.3.v20150130.jar this ALPN-Version
 * should work with java 1.7 download at
 * https://mvnrepository.com/artifact/org.mortbay.jetty.alpn/alpn-boot
 * 
 *
 */
public class PushyTest {

	/**
	 * @param args
	 * @throws IOException
	 * @throws SSLException
	 * @throws InterruptedException
	 */
	public static void main(final String[] args) throws SSLException, IOException, InterruptedException {

		final ApnsClient apnsClient = new ApnsClientBuilder().setClientCredentials(new File("cert.p12"), "umdapp")
				.build();

		final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
		connectFuture.await();

		final SimpleApnsPushNotification pushNotification;

		{
			final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
			payloadBuilder.setAlertBody("{messageCode: 4, accountID: FA5EF}");

			final String payload = payloadBuilder.buildWithDefaultMaximumLength();
			final String token = TokenUtil.sanitizeTokenString("<replace_this_with your_token>");

			pushNotification = new SimpleApnsPushNotification(token, "de.tgic.umd", payload);
		}

		final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = apnsClient
				.sendNotification(pushNotification);

		try {
			final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = sendNotificationFuture
					.get();

			if (pushNotificationResponse.isAccepted()) {
				System.out.println("Push notification accepted by APNs gateway.");
			} else {
				System.out.println(
						"Notification rejected by the APNs gateway: " + pushNotificationResponse.getRejectionReason());

				if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
					System.out.println("\t…and the token is invalid as of "
							+ pushNotificationResponse.getTokenInvalidationTimestamp());
				}
			}
		} catch (final ExecutionException e) {
			System.err.println("Failed to send push notification.");
			e.printStackTrace();

			if (e.getCause() instanceof ClientNotConnectedException) {
				System.out.println("Waiting for client to reconnect…");
				apnsClient.getReconnectionFuture().await();
				System.out.println("Reconnected.");
			}
		}

	}
}
