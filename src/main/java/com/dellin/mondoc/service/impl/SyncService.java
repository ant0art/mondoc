package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.pojo.DocumentRequest;
import com.dellin.mondoc.model.pojo.DocumentResponse;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {
	@Value("${api.address}")
	String baseUrlFid = "";

	private IInterfaceManualLoad iInterfaceManualLoad;

	public IInterfaceManualLoad getRemoteData() {

		Gson gson = new GsonBuilder()
				.setLenient()
				.create();

		if (iInterfaceManualLoad == null) {
			Retrofit retrofit = new Retrofit.Builder()
					.baseUrl(baseUrlFid)
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
					.addConverterFactory(GsonConverterFactory.create(gson))
					.client(getUnsafeOkHttpClient())
					.build();
			iInterfaceManualLoad = retrofit.create(IInterfaceManualLoad.class);
		}
		return iInterfaceManualLoad;
	}

	public interface IInterfaceManualLoad {
		@POST("/v3/auth/login.json")
		Call<AuthDellin> login(@Body SessionDTO sessionDTO);

		@POST("/v3/auth/logout.json")
		Call<AuthDellin> logout(@Body SessionDTO sessionDTO);

		@POST("/v3/orders.json")
		Call<OrderResponse> update(@Body OrderRequest orderRequest);

		@POST("/v1/printable.json")
		Call<DocumentResponse> getPrintableDoc(@Body DocumentRequest documentRequest);
	}

	private OkHttpClient getUnsafeOkHttpClient() {
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[]{
					new X509TrustManager() {

						public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws
								CertificateException {

						}

						public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

						}

						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[]{};
						}
					}
			};

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
