package group.artifact;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

/**
 * This is a try to figure out how it is possible to use Apple Provider API with
 * Java 1.7
 *
 */
public class App {

	static final boolean SSL = System.getProperty("ssl") != null;
	static final String HOST = System.getProperty("host", "api.development.push.apple.com");
	static final int PORT = Integer.parseInt(System.getProperty("port", "443"));
	static final String URL = System.getProperty("url", "api.development.push.apple.com");
	static final String URL2 = System.getProperty("url2");
	static final String URL2DATA = System.getProperty("url2data", "test data!");

	// In order to run this, you need the alpn-boot-XXX.jar in the bootstrap
	// classpath.
	// In order to run this, you need jetty 9.0.x
	// see at http://de.slideshare.net/RomanTereschenko1/jettypresentation-3
	public static void main(final String[] args) throws Exception {

		System.out.println("Start Apple Push Test!");

		final OkHttpClient client = App.getSafeClient();
		String adress = "https://" + HOST + ":" + PORT;
		System.out.println(adress);
		final Request request = new Request.Builder().url(adress).build();

		// The Http2Server should be running here.
		final long startTime = System.nanoTime();

		for (int i = 0; i < 1; i++) {
			Thread.sleep(1000);
			// http://stackoverflow.com/questions/32625035/when-using-http2-in-okhttp-why-multi-requests-to-the-same-host-didnt-use-just
			client.newCall(request).enqueue(new Callback() {

				public void onFailure(Call call, IOException e) {
					e.printStackTrace();
				}

				public void onResponse(Call call, Response response) throws IOException {
					final long duration = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
					System.out.println("After " + duration + " seconds: " + response.body().string());
				}

			});
		}
	}

	// http://stackoverflow.com/questions/25509296/trusting-all-certificates-with-okhttp
	private static OkHttpClient getUnsafeOkHttpClient() {

		try {
			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, SslContextFactory.TRUST_ALL_CERTS, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			final OkHttpClient okHttpClient = new OkHttpClient();
			okHttpClient.sslSocketFactory().createSocket();
			// okHttpClient.setSslSocketFactory(sslSocketFactory);
			// okHttpClient.setHostnameVerifier((hostname, session) -> true);

			return okHttpClient;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	// see@ https://github.com/square/okhttp/wiki/HTTPS
	private static OkHttpClient getSafeClient() {

		try {
			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, SslContextFactory.TRUST_ALL_CERTS, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			// final SSLSocketFactory sslSocketFactory =
			// sslContext.getSocketFactory();

			ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2)
					.cipherSuites(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
							CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
							CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
							CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256)
					.build();

			OkHttpClient okHttpClient = new OkHttpClient.Builder().connectionSpecs(Collections.singletonList(spec))
					.build();
			okHttpClient.sslSocketFactory().createSocket();

			return okHttpClient;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
