/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.messaging.converter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.Test;

import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeType;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test fixture for {@link org.springframework.messaging.converter.MappingJackson2MessageConverter}.
 *
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 */
public class MappingJackson2MessageConverterTests {

	private static Charset UTF_8 = Charset.forName("UTF-8");


	@Test
	public void defaultConstructor() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		assertThat(converter.getSupportedMimeTypes(), contains(new MimeType("application", "json", UTF_8)));
		assertFalse(converter.getObjectMapper().getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
	}

	@Test  // SPR-12724
	public void mimetypeParametrizedConstructor() {
		MimeType mimetype = new MimeType("application", "xml", UTF_8);
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter(mimetype);
		assertThat(converter.getSupportedMimeTypes(), contains(mimetype));
		assertFalse(converter.getObjectMapper().getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
	}

	@Test  // SPR-12724
	public void mimetypesParametrizedConstructor() {
		MimeType jsonMimetype = new MimeType("application", "json", UTF_8);
		MimeType xmlMimetype = new MimeType("application", "xml", UTF_8);
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter(jsonMimetype, xmlMimetype);
		assertThat(converter.getSupportedMimeTypes(), contains(jsonMimetype, xmlMimetype));
		assertFalse(converter.getObjectMapper().getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
	}

	@Test
	public void fromMessage() throws Exception {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		String payload = "{\"bytes\":\"AQI=\",\"array\":[\"Foo\",\"Bar\"],"
				+ "\"number\":42,\"string\":\"Foo\",\"bool\":true,\"fraction\":42.0}";
		Message<?> message = MessageBuilder.withPayload(payload.getBytes(UTF_8)).build();
		MyBean actual = (MyBean) converter.fromMessage(message, MyBean.class);

		assertEquals("Foo", actual.getString());
		assertEquals(42, actual.getNumber());
		assertEquals(42F, actual.getFraction(), 0F);
		assertArrayEquals(new String[]{"Foo", "Bar"}, actual.getArray());
		assertTrue(actual.isBool());
		assertArrayEquals(new byte[]{0x1, 0x2}, actual.getBytes());
	}

	@Test
	public void fromMessageUntyped() throws Exception {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		String payload = "{\"bytes\":\"AQI=\",\"array\":[\"Foo\",\"Bar\"],"
				+ "\"number\":42,\"string\":\"Foo\",\"bool\":true,\"fraction\":42.0}";
		Message<?> message = MessageBuilder.withPayload(payload.getBytes(UTF_8)).build();
		@SuppressWarnings("unchecked")
		HashMap<String, Object> actual = (HashMap<String, Object>) converter.fromMessage(message, HashMap.class);

		assertEquals("Foo", actual.get("string"));
		assertEquals(42, actual.get("number"));
		assertEquals(42D, (Double) actual.get("fraction"), 0D);
		assertEquals(Arrays.asList("Foo", "Bar"), actual.get("array"));
		assertEquals(Boolean.TRUE, actual.get("bool"));
		assertEquals("AQI=", actual.get("bytes"));
	}

	@Test(expected = MessageConversionException.class)
	public void fromMessageInvalidJson() throws Exception {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		String payload = "FooBar";
		Message<?> message = MessageBuilder.withPayload(payload.getBytes(UTF_8)).build();
		converter.fromMessage(message, MyBean.class);
	}

	@Test
	public void fromMessageValidJsonWithUnknownProperty() throws IOException {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		String payload = "{\"string\":\"string\",\"unknownProperty\":\"value\"}";
		Message<?> message = MessageBuilder.withPayload(payload.getBytes(UTF_8)).build();
		MyBean myBean = (MyBean)converter.fromMessage(message, MyBean.class);
		assertEquals("string", myBean.getString());
	}

	@Test
	public void toMessage() throws Exception {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		MyBean payload = new MyBean();
		payload.setString("Foo");
		payload.setNumber(42);
		payload.setFraction(42F);
		payload.setArray(new String[]{"Foo", "Bar"});
		payload.setBool(true);
		payload.setBytes(new byte[]{0x1, 0x2});

		Message<?> message = converter.toMessage(payload, null);
		String actual = new String((byte[]) message.getPayload(), UTF_8);

		assertTrue(actual.contains("\"string\":\"Foo\""));
		assertTrue(actual.contains("\"number\":42"));
		assertTrue(actual.contains("fraction\":42.0"));
		assertTrue(actual.contains("\"array\":[\"Foo\",\"Bar\"]"));
		assertTrue(actual.contains("\"bool\":true"));
		assertTrue(actual.contains("\"bytes\":\"AQI=\""));
		assertEquals("Invalid content-type", new MimeType("application", "json", UTF_8),
				message.getHeaders().get(MessageHeaders.CONTENT_TYPE, MimeType.class));
	}

	@Test
	public void toMessageUtf16() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		Charset utf16 = Charset.forName("UTF-16BE");
		MimeType contentType = new MimeType("application", "json", utf16);
		Map<String, Object> map = new HashMap<>();
		map.put(MessageHeaders.CONTENT_TYPE, contentType);
		MessageHeaders headers = new MessageHeaders(map);
		String payload = "H\u00e9llo W\u00f6rld";
		Message<?> message = converter.toMessage(payload, headers);

		assertEquals("\"" + payload + "\"", new String((byte[]) message.getPayload(), utf16));
		assertEquals(contentType, message.getHeaders().get(MessageHeaders.CONTENT_TYPE));
	}

	@Test
	public void toMessageUtf16String() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setSerializedPayloadClass(String.class);

		Charset utf16 = Charset.forName("UTF-16BE");
		MimeType contentType = new MimeType("application", "json", utf16);
		Map<String, Object> map = new HashMap<>();
		map.put(MessageHeaders.CONTENT_TYPE, contentType);
		MessageHeaders headers = new MessageHeaders(map);
		String payload = "H\u00e9llo W\u00f6rld";
		Message<?> message = converter.toMessage(payload, headers);

		assertEquals("\"" + payload + "\"", message.getPayload());
		assertEquals(contentType, message.getHeaders().get(MessageHeaders.CONTENT_TYPE));
	}

	@Test
	public void jsonView() throws Exception {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();

		Map<String, Object> map = new HashMap<>();
		Method method = this.getClass().getDeclaredMethod("handle");
		MethodParameter returnType = new MethodParameter(method, -1);
		map.put(AbstractMessageConverter.METHOD_PARAMETER_HINT_HEADER, returnType);
		MessageHeaders headers = new MessageHeaders(map);
		Message<?> message = converter.toMessage(handle(), headers);
		String actual = new String((byte[]) message.getPayload(), UTF_8);

		assertThat(actual, containsString("\"withView1\":\"with\""));
		assertThat(actual, not(containsString("\"withView2\":\"with\"")));
		assertThat(actual, not(containsString("\"withoutView\":\"without\"")));
	}


	public static class MyBean {

		private String string;

		private int number;

		private float fraction;

		private String[] array;

		private boolean bool;

		private byte[] bytes;

		public byte[] getBytes() {
			return bytes;
		}

		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}

		public boolean isBool() {
			return bool;
		}

		public void setBool(boolean bool) {
			this.bool = bool;
		}

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public float getFraction() {
			return fraction;
		}

		public void setFraction(float fraction) {
			this.fraction = fraction;
		}

		public String[] getArray() {
			return array;
		}

		public void setArray(String[] array) {
			this.array = array;
		}
	}

	public interface MyJacksonView1 {};
	public interface MyJacksonView2 {};

	public static class JacksonViewBean {

		@JsonView(MyJacksonView1.class)
		private String withView1;

		@JsonView(MyJacksonView2.class)
		private String withView2;

		private String withoutView;

		public String getWithView1() {
			return withView1;
		}

		public void setWithView1(String withView1) {
			this.withView1 = withView1;
		}

		public String getWithView2() {
			return withView2;
		}

		public void setWithView2(String withView2) {
			this.withView2 = withView2;
		}

		public String getWithoutView() {
			return withoutView;
		}

		public void setWithoutView(String withoutView) {
			this.withoutView = withoutView;
		}
	}

	@JsonView(MyJacksonView1.class)
	public JacksonViewBean handle() {
		JacksonViewBean bean = new JacksonViewBean();
		bean.setWithView1("with");
		bean.setWithView2("with");
		bean.setWithoutView("without");
		return bean;
	}

}
