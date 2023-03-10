package com.dellin.mondoc.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * The Retrofit service class
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {
	
	/**
	 * Base url of API Dellin
	 */
	@Value("${api.address}")
	String baseUrlFid = "";
	
	/**
	 * Injection of Retrofit interface of API Dellin requests
	 */
	private IInterfaceManualLoad iInterfaceManualLoad;
	
	/**
	 * Method that initialize the Retrofit interface with base url of API Dellin
	 * <p>
	 * Returns the Retrofit interface with basic url and GsonConverter
	 *
	 * @return the {@link IInterfaceManualLoad} Retrofit interface
	 */
	public IInterfaceManualLoad getRemoteData() {
		
		Gson gson = new GsonBuilder().setLenient().create();
		
		if (iInterfaceManualLoad == null) {
			Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrlFid)
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
					.addConverterFactory(GsonConverterFactory.create(gson))
					.client(getUnsafeOkHttpClient()).build();
			iInterfaceManualLoad = retrofit.create(IInterfaceManualLoad.class);
		}
		return iInterfaceManualLoad;
	}
	
	/**
	 * Method that builds the OkHttpClient
	 *
	 * @return the {@link OkHttpClient} object
	 */
	private OkHttpClient getUnsafeOkHttpClient() {
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts =
					new TrustManager[]{new X509TrustManager() {
						
						public void checkClientTrusted(X509Certificate[] x509Certificates,
								String s) throws CertificateException {
							
						}
						
						public void checkServerTrusted(X509Certificate[] x509Certificates,
								String s) throws CertificateException {
							
						}
						
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[]{};
						}
					}};
			
			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			
			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			
			builder.readTimeout(240, TimeUnit.SECONDS);
			builder.connectTimeout(240, TimeUnit.SECONDS);
			builder.writeTimeout(240, TimeUnit.SECONDS);
			
			//   builder.sslSocketFactory(sslSocketFactory);
			builder.hostnameVerifier(new HostnameVerifier() {
				public boolean verify(String s, SSLSession sslSession) {
					return true;
				}
			});
			
			OkHttpClient okHttpClient = builder.build();
			return okHttpClient;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
