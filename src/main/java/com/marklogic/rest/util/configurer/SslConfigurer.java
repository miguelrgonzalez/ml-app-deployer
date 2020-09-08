package com.marklogic.rest.util.configurer;

import com.marklogic.client.ext.helper.LoggingObject;
import com.marklogic.client.ext.ssl.SslUtil;
import com.marklogic.rest.util.HttpClientBuilderConfigurer;
import com.marklogic.rest.util.RestConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;

public class SslConfigurer extends LoggingObject implements HttpClientBuilderConfigurer {

	/**
	 * First checks for a custom SSLContext; then checks to see if the default keystore should be used; then checks to
	 * see if a simple "trust everything" approach should be used.
	 *
	 * @param config
	 * @param httpClientBuilder
	 * @return
	 */
	@Override
	public HttpClientBuilder configureHttpClientBuilder(RestConfig config, HttpClientBuilder httpClientBuilder) {
		SSLContext sslContext = null;
		if (config.getSslContext() != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Using custom SSLContext for connecting to: " + config.getBaseUrl());
			}
			sslContext = config.getSslContext();
		} else if (config.isUseDefaultKeystore()) {
			sslContext = buildSslContextViaTrustManagerFactory(config);
		} else if (config.isConfigureSimpleSsl()) {
			sslContext = buildSimpleSslContext(config);
		}

		if (sslContext != null) {
			httpClientBuilder.setSslcontext(sslContext);

			if (config.getHostnameVerifier() != null) {
				if (logger.isInfoEnabled()) {
					logger.info("Using custom X509HostnameVerifier for connecting to: " + config.getBaseUrl());
				}
				httpClientBuilder.setHostnameVerifier(config.getHostnameVerifier());
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("Using 'allow all' X509HostnameVerifier for connecting to: " + config.getBaseUrl());
				}
				httpClientBuilder.setHostnameVerifier(new AllowAllHostnameVerifier());
			}
		}

		return httpClientBuilder;
	}

	protected SSLContext buildSslContextViaTrustManagerFactory(RestConfig config) {
		final String protocol = determineProtocol(config);
		final String algorithm = config.getTrustManagementAlgorithm();
		if (logger.isInfoEnabled()) {
			logger.info("Using default keystore with SSL protocol " + protocol + " for connecting to: " + config.getBaseUrl());
		}
		return SslUtil.configureUsingTrustManagerFactory(protocol, algorithm).getSslContext();
	}

	protected SSLContext buildSimpleSslContext(RestConfig config) {
		final String protocol = determineProtocol(config);

		SSLContextBuilder builder = new SSLContextBuilder().useProtocol(protocol);
		if (logger.isInfoEnabled()) {
			logger.info("Configuring simple SSL approach with protocol " + protocol + " for connecting to: " + config.getBaseUrl());
		}
		try {
			return builder.loadTrustMaterial(null, (chain, authType) -> true).build();
		} catch (Exception ex) {
			throw new RuntimeException("Unable to configure simple SSLContext for connecting to: " + config.getBaseUrl() + ", cause: " + ex.getMessage(), ex);
		}
	}

	protected String determineProtocol(RestConfig config) {
		String protocol = config.getSslProtocol();
		if (StringUtils.isEmpty(protocol)) {
			protocol = SslUtil.DEFAULT_SSL_PROTOCOL;
		}
		return protocol;
	}
}
