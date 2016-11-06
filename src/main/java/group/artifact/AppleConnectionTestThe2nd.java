package group.artifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.CipherSuite;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.TlsVersion;

/**
 * needed VM-arguments -Djavax.net.debug=all -Dhttps.protocols=TLSv1.2
 * -Ddeployment.security.TLSv1.2=true
 * -Dsun.security.ssl.allowLegacyHelloMessages=true
 * -Dsun.security.ssl.allowUnsafeRenegotiation=true
 * 
 * dont use-Xbootclasspath/p:C:/alpn/alpn-boot-7.1.3.v20150130.jar its not
 * necessary with okhttp
 *
 */
public class AppleConnectionTestThe2nd {

	static final int PORT = Integer.parseInt(System.getProperty("port", "443"));
	static final String URL2DATA = System.getProperty("url2data", "test data!");
	private static final String TRUST_STORE_PASSWORD = "your_pw";

	public static final String ENDPOINT_PRODUCTION = "https://api.push.apple.com";
	public static final String ENDPOINT_SANDBOX = "https://api.development.push.apple.com";

	// In order to run this, you need jetty 9.0.x
	public static void main(final String[] args) throws Exception {

		System.out.println("Start Apple Push Test!");

		ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2)
				.cipherSuites(CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
						CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
						CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA,
						CipherSuite.TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA, CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
						CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA,
						CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA,
						CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,
						CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,
						CipherSuite.TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,
						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
						CipherSuite.TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA, CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,
						CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,
						CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
						CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
						CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256)
				.build();

		// create Client
		OkHttpClient client = new OkHttpClient();
		client.setConnectionSpecs(Collections.singletonList(spec));

		// read in certificate
		KeyStore trusted = KeyStore.getInstance("PKCS12");
		FileInputStream p12InputStream = new FileInputStream(new File("cert.p12"));
		trusted.load(p12InputStream, TRUST_STORE_PASSWORD.toCharArray());

		// get SSLContext
		javax.net.ssl.SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		// Get the list of all supported cipher suites
		// printOutAllSupportedCiphers();

		// SSLSocketFactory sslContext = client.sslSocketFactory();
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trusted);
		sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

		String adress = ENDPOINT_SANDBOX + ":" + PORT;
		System.out.println(adress);
		final Request request = new Request.Builder().url(adress).build();

		// The Http2Server should be running here.
		final long startTime = System.nanoTime();

		client.newCall(request).enqueue(new Callback() {

			public void onFailure(Request arg0, IOException e) {
				e.printStackTrace();

			}

			public void onResponse(Response response) throws IOException {
				final long duration = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
				System.out.println("After " + duration + " seconds: " + response.body().string());

			}

		});
	}

	private static void printOutAllSupportedCiphers() throws IOException {
		SSLServerSocketFactory ssl;
		SSLServerSocket sslServerSocket;
		ssl = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		sslServerSocket = (SSLServerSocket) ssl.createServerSocket();

		// Get the list of all supported cipher suites.
		String[] cipherSuites = sslServerSocket.getSupportedCipherSuites();

		for (String suite : cipherSuites)
			System.out.println(suite);

		System.out.println("************************************************************");
	}

}
