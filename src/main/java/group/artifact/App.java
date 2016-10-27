package group.artifact;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * This is a try to figure out how it is possible to use Apple Provider API with Java 1.7
 *
 */
public class App 
{


	static final boolean	SSL			= System.getProperty( "ssl" ) != null;
	static final String		HOST		= System.getProperty( "host", "api.development.push.apple.com" );
	static final int		PORT		= Integer.parseInt( System.getProperty( "port", "443" ) );
	static final String		URL			= System.getProperty( "url", "api.development.push.apple.com" );
	static final String		URL2		= System.getProperty( "url2" );
	static final String		URL2DATA	= System.getProperty( "url2data", "test data!" );

	// In order to run this, you need the alpn-boot-XXX.jar in the bootstrap classpath.
	public static void
	main(
		final String[ ] args )
	throws Exception {
		
	    System.out.println( "Start Apple Push Test!" );

    		final OkHttpClient client = App.getUnsafeOkHttpClient( );
    		final Request request = new Request.Builder( ).url( "https://localhost:8443" ) // The Http2Server should be
    																						// running here.
    			.build( );
    		final long startTime = System.nanoTime( );
    		for( int i = 0; i < 3; i++ ) {
    			Thread.sleep( 1000 ); // http://stackoverflow.com/questions/32625035/when-using-http2-in-okhttp-why-multi-requests-to-the-same-host-didnt-use-just
    			client.newCall( request ).enqueue( new Callback( ) {

    				public void
    				onFailure(
    					final Request request,
    					final IOException e ) {

    					e.printStackTrace( );
    				}

    				public void
    				onResponse(
    					final Response response )
    				throws IOException {

    					final long duration = TimeUnit.NANOSECONDS.toSeconds( System.nanoTime( ) - startTime );
    					System.out.println( "After " + duration + " seconds: " + response.body( ).string( ) );
    				}

					public void onFailure(Call arg0, IOException arg1) {
						// TODO Auto-generated method stub
						
					}

					public void onResponse(Call arg0, Response arg1) throws IOException {
						// TODO Auto-generated method stub
						
					}

    			} );
    		}
    	}

    	// http://stackoverflow.com/questions/25509296/trusting-all-certificates-with-okhttp
    	private static OkHttpClient
    	getUnsafeOkHttpClient( ) {

    		try {
    			// Install the all-trusting trust manager
    			final SSLContext sslContext = SSLContext.getInstance( "SSL" );
    			sslContext.init( null, SslContextFactory.TRUST_ALL_CERTS, new java.security.SecureRandom( ) );
    			// Create an ssl socket factory with our all-trusting manager
    			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory( );

    			final OkHttpClient okHttpClient = new OkHttpClient( );
    			okHttpClient.sslSocketFactory( ).createSocket( );
    			// okHttpClient.setSslSocketFactory(sslSocketFactory);
    			// okHttpClient.se.setHostnameVerifier((hostname, session) -> true);

    			return okHttpClient;
    		} catch( final Exception e ) {
    			throw new RuntimeException( e );
    		}
    	}

}