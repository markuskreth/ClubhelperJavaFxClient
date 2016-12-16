package de.kreth.clubhelperclient;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import de.kreth.clubhelperbackend.pojo.Data;
import de.kreth.clubhelperclient.core.RemoteHolder;
import de.kreth.encryption.Encryptor;

public abstract class Repository<T extends Data> {

	public final static String USER_AGENT = "Clubhelper JavaFx Client";

	private final Class<T> typeClass;
	private final Class<T[]> listClass;

	private Encryptor encryptor = new Encryptor();

	private RemoteHolder remoteHolder;

	public Repository(Class<T> typeClass, Class<T[]> listClass) {
		super();
		this.typeClass = typeClass;
		this.listClass = listClass;
	}

	@Autowired
	public void setRemoteHolder(RemoteHolder remoteHolder) {
		this.remoteHolder = remoteHolder;

	}

	public List<T> all() throws IOException {

		RestTemplate restTemplate;

		try {
			restTemplate = createRestTemplate();
			ResponseEntity<T[]> all = restTemplate.getForEntity(getBaseUrl(), listClass);
			return Arrays.asList(all.getBody());
		} catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException
				| org.springframework.web.client.ResourceAccessException e) {
			throw new IOException(e);
		}

	}

	private String getBaseUrl() {
		String remoteUrl = remoteHolder.getRemoteUrl();
		String simpleName = typeClass.getSimpleName().toLowerCase();
		return remoteUrl + "/" + simpleName;
	}

	public T getById(long id) throws IOException {

		RestTemplate restTemplate;

		try {
			restTemplate = createRestTemplate();
			ResponseEntity<T> entity = restTemplate.getForEntity(getBaseUrl() + "/" + id, typeClass);
			return entity.getBody();
		} catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
			throw new IOException(e);
		}

	}

	public List<T> getByParentId(long id) throws IOException {

		RestTemplate restTemplate;

		try {
			restTemplate = createRestTemplate();
			ResponseEntity<T[]> all = restTemplate.getForEntity(getBaseUrl() + "/for/" + id, listClass);
			return Arrays.asList(all.getBody());
		} catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
			throw new IOException(e);
		}

	}

	public void delete(T obj) throws IOException {
		RestTemplate restTemplate;

		try {
			restTemplate = createRestTemplate();
			restTemplate.delete(getBaseUrl() + "/" + obj.getId(), obj);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		}
	}

	public void update(T obj) throws IOException {
		RestTemplate restTemplate;
		try {
			restTemplate = createRestTemplate();
			restTemplate.put(getBaseUrl() + "/" + obj.getId(), obj);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		}

	}

	public T insert(T obj) throws IOException {

		RestTemplate restTemplate;
		T result = null;

		try {
			restTemplate = createRestTemplate();
			ResponseEntity<T> entity = restTemplate.postForEntity(getBaseUrl() + "/" + obj.getId(), obj, typeClass);
			result = entity.getBody();
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		}

		return result;
	}

	private RestTemplate createRestTemplate()
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Date now = new Date();

		RestTemplate restTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		// interceptors.add(new HeaderRequestInterceptor("Content-Type",
		// "application/json"));
		interceptors.add(new HeaderRequestInterceptor("Accept", "application/json"));
		interceptors.add(new HeaderRequestInterceptor("User-Agent", USER_AGENT));
		interceptors.add(new HeaderRequestInterceptor("localtime", String.valueOf(now.getTime())));
		interceptors.add(new HeaderRequestInterceptor("token", encryptor.encrypt(now, USER_AGENT)));
		restTemplate.setInterceptors(interceptors);

		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.getObjectMapper().setDateFormat(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS Z"));

		messageConverters.add(converter);
		restTemplate.setMessageConverters(messageConverters);

		return restTemplate;
	}

	class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {

		/** */
		private final String headerName;

		/** */
		private final String headerValue;

		/**
		 * 
		 * @param headerName
		 * @param headerValue
		 */
		public HeaderRequestInterceptor(String headerName, String headerValue) {
			this.headerName = headerName;
			this.headerValue = headerValue;
		}

		/**
		 * 
		 * @param request
		 * @param body
		 * @param execution
		 * @return @throws
		 */
		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			HttpRequest wrapper = new HttpRequestWrapper(request);
			wrapper.getHeaders().set(headerName, headerValue);
			return execution.execute(wrapper, body);
		}
	}

}
