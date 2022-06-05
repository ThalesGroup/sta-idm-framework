/**
 * Copyright (c) 2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.connid.sta.connector;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import com.evolveum.polygon.rest.AbstractRestConnector;
import org.identityconnectors.common.Base64;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.*;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;

import com.evolveum.polygon.rest.AbstractRestConnector;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author semancik
 *
 */
@ConnectorClass(displayNameKey = "connector.example.rest.display", configurationClass = STARestConfiguration.class)
public class STARestConnector extends AbstractRestConnector<STARestConfiguration> implements TestOp, SchemaOp, SearchOp<staFilter> {

	private static final Log LOG = Log.getLog(STARestConnector.class);

	//private static final String TENANT = "/tenants/"
	private static final String USER = "/users";
	private static final String BASE = "/api/v1/tenants/";

	//attribute names
	private static final String ATTR_UID = "id";
	private static final String ATTR_SCHEMAVERSION = "schemaVersionNumber";
	private static final String ATTR_UNAME = "userName";
	private static final String ATTR_FIRSTNAME = "firstName";
	private static final String ATTR_LASTNAME = "lastName";
	private static final String ATTR_MAIL = "email";

	private static final String CONTENT_TYPE = "application/json";

	@Override
	public void test() {
		URIBuilder uriBuilder = getURIBuilder();
		URI uri;
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		HttpGet request = new HttpGet(uri);
		//request.setHeader("apikey", Con);

		CloseableHttpResponse response = execute(request);

		processResponseErrors(response);
	}

	@Override
	public Schema schema() {
		SchemaBuilder schemaBuilder = new SchemaBuilder(STARestConnector.class);
		//ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
		//schemaBuilder.defineObjectClass(ocBuilder.build());
		buildAccountObjectClass(schemaBuilder);
		return schemaBuilder.build();
	}


	private void buildAccountObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder objClassBuilder = new ObjectClassInfoBuilder();

		// UID & NAME are defaults
/*
  "id": "BAAAAAAAAAAFAAAAAAAAAAAB",
                "schemaVersionNumber": "1.0",
                "userName": "bxu",
                "firstName": "Bin",
                "lastName": "Xu",
                "email": "bxu027@gmail.com",
                "isSynchronized": false,
                "isActive": true
 */
		AttributeInfoBuilder attrMailBuilder = new AttributeInfoBuilder("id");
		attrMailBuilder.setRequired(true);
		//attrMailBuilder.setReturnedByDefault(true); // TODO: list not returned by default, only user details
		objClassBuilder.addAttributeInfo(attrMailBuilder.build());

		AttributeInfoBuilder attrSchemaversionBuilder = new AttributeInfoBuilder(ATTR_SCHEMAVERSION);
		objClassBuilder.addAttributeInfo(attrSchemaversionBuilder.build());

		AttributeInfoBuilder attrUsernameBuilder = new AttributeInfoBuilder(ATTR_UNAME);
		attrUsernameBuilder.setRequired(true);
		objClassBuilder.addAttributeInfo(attrUsernameBuilder.build());

		AttributeInfoBuilder attrfirstNameBuilder = new AttributeInfoBuilder(ATTR_FIRSTNAME);
		objClassBuilder.addAttributeInfo(attrfirstNameBuilder.build());

		AttributeInfoBuilder attrlastNameBuilder = new AttributeInfoBuilder(ATTR_LASTNAME);
		objClassBuilder.addAttributeInfo(attrlastNameBuilder.build());

		AttributeInfoBuilder attremailBuilder = new AttributeInfoBuilder(ATTR_MAIL);
		objClassBuilder.addAttributeInfo(attremailBuilder.build());


		schemaBuilder.defineObjectClass(objClassBuilder.build());
	}

	public FilterTranslator<staFilter> createFilterTranslator(ObjectClass objectClass, OperationOptions operationOptions) {
		return new staFilterTranslator();
	}

	public void executeQuery(ObjectClass objectClass, staFilter query, ResultsHandler handler, OperationOptions options) {
		try {
			LOG.info("executeQuery on {0}, query: {1}, options: {2}", objectClass, query, options);
			if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
				//find by Uid (user Primary Key)
				if (query != null && query.byUid != null) {
					HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + BASE + getConfiguration().gettenantcode() + USER + "/" + query.byUid + "?isuid=true" );

					JSONObject user = callRequest(request, true);
					ConnectorObject connectorObject = convertUserToConnectorObject(user);
					handler.handle(connectorObject);
				}// find by name
				else if (query != null && query.byName != null) {
					HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + BASE + getConfiguration().gettenantcode() + USER  + "/" + query.byName);
					JSONObject user = callRequest(request, true);
					ConnectorObject connectorObject = convertUserToConnectorObject(user);
					handler.handle(connectorObject);
					//handleUsers(request, handler, options, false);

					//find by emailAddress
				    //} else if (query != null && query.byEmailAddress != null) {
					//HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + BASE + getConfiguration().gettenantcode() + USER + "?parameters[" + ATTR_MAIL + "]=" + query.byEmailAddress);
					//handleUsers(request, handler, options, false);

				} else {
					// find required page
					String pageing = processPageOptions(options);
					if (!StringUtil.isEmpty(pageing)) {
						//check paging
						HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + BASE + getConfiguration().gettenantcode() + USER  + "?" + pageing);

						handleUsers(request, handler, options, false);
					}
					// find all
					else {
						int pageSize = 5 ; // hardocoded value getConfiguration().getPageSize();
						int page = 0;
						while (true) {
							pageing = processPaging(page, pageSize);
							HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + BASE + getConfiguration().gettenantcode() + USER + "?" + pageing);
							boolean finish = handleUsers(request, handler, options, true);
							if (finish) {
								break;
							}
							page++;
						}
					}
				}

			}
		} catch (IOException e) {
			throw new ConnectorIOException(e.getMessage(), e);
		}
	}

	private String processPageOptions(OperationOptions options) {
		if (options != null) {
			Integer pageSize = 5;
			Integer pagedResultsOffset = 0;
			if (pageSize != null && pagedResultsOffset != null) {

				return processPaging(pagedResultsOffset, pageSize);
			}
		}
		return "";
	}
	public String processPaging(int page, int pageSize) {
		StringBuilder queryBuilder = new StringBuilder();
		LOG.ok("creating paging with page: {0}, pageSize: {1}", page, pageSize);
		queryBuilder.append("&pageindex=").append(page).append("&").append("pagesize=")
				.append(pageSize);

		return queryBuilder.toString();
	}

	public boolean handleUsers(HttpGet request, ResultsHandler handler, OperationOptions options, boolean findAll) throws IOException {

		JSONArray users = callRequest(request);
		//LOG.ok("Number of users: {0}, pageResultsOffset: {1}, pageSize: {2} ", users.length(), options == null ? "null" : options.getPagedResultsOffset(), options == null ? "null" : 5);

		for (int i = 0; i < users.length(); i++) {
			if (i % 10 == 0) {
				LOG.ok("executeQuery: processing {0}. of {1} users", i, users.length());
			}
			// only basic fields
			JSONObject user = users.getJSONObject(i);
			/*if (this.getConfiguration().getDontReadUserDetailsWhenFindAllUsers() && findAll){
				if (i % user.length() == 0) {
					LOG.ok("DontReadUserDetailsWhenFindAllUsers property is enabled and finnAll is catched - ignoring reading user details");
				}
			}
			else if (getConfiguration().getUserMetadatas().size() > 1) {
				// when using extended fields we need to get it each by one
				HttpGet requestUserDetail = new HttpGet(getConfiguration().getServiceAddress() + USER + "/" + user.getString(ATTR_UNAME));

				user = callRequest(requestUserDetail, true);
			}*/

			ConnectorObject connectorObject = convertUserToConnectorObject(user);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return true;
			}
		}

		// last page exceed
		if (5 > users.length()) {
			return true;
		}
		// need next page
		return false;
	}
	protected JSONArray callRequest(HttpRequestBase request) throws IOException {
		LOG.ok("request URI: {0}", request.getURI());
		request.setHeader("Content-Type", CONTENT_TYPE);

		authHeader(request);

		CloseableHttpResponse response = execute(request);
		LOG.warn("response: {0}", response);
		//processDrupalResponseErrors(response);

		String result = EntityUtils.toString(response.getEntity());
		LOG.error("response body: {0}", result);
		closeResponse(response);
		JSONObject responsjson = new JSONObject(result);
		JSONObject inside=  (JSONObject)responsjson.get("page");

		return ((JSONArray)inside.get("items"));
	}

	protected JSONObject callRequest(HttpRequestBase request, boolean parseResult) throws IOException {
		LOG.error("request URI: {0}", request.getURI());
		request.setHeader("Content-Type", CONTENT_TYPE);

		authHeader(request);

		CloseableHttpResponse response = null;
		response = execute(request);
		LOG.error("response: {0}", response);
		//processDrupalResponseErrors(response);

		if (!parseResult) {
			closeResponse(response);
			return null;
		}
		String result = EntityUtils.toString(response.getEntity());
		LOG.error("response body: {0}", result);
		closeResponse(response);
		return new JSONObject(result);
	}

	private void authHeader(HttpRequestBase request){
		// to prevent several calls http://stackoverflow.com/questions/20914311/httpclientbuilder-basic-auth
		// auth header
		request.setHeader("apikey", getConfiguration().getapikey());
	}

	private ConnectorObject convertUserToConnectorObject(JSONObject user) throws IOException {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(user.getString(ATTR_UID)));
		if (user.has(ATTR_UNAME)) {
			builder.setName(user.getString(ATTR_UNAME));
		}
		getIfExists(user, ATTR_MAIL, builder);
		getIfExists(user, ATTR_SCHEMAVERSION, builder);
		getIfExists(user, ATTR_FIRSTNAME, builder);
		getIfExists(user, ATTR_LASTNAME, builder);


/*
		if (user.has(ATTR_ROLES)) {
			JSONObject roles = user.getJSONObject(ATTR_ROLES);
			String[] roleArray = roles.keySet().toArray(new String[roles.keySet().size()]);
			builder.addAttribute(ATTR_ROLES, roleArray);
		}
*/
		ConnectorObject connectorObject = builder.build();
		LOG.ok("convertUserToConnectorObject, user: {0}, \n\tconnectorObject: {1}",
				user.getString(ATTR_UID), connectorObject);
		return connectorObject;
	}

	private void getIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
		if (object.has(attrName)) {
			if (object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
				addAttr(builder, attrName, object.getString(attrName));
			}
		}
	}


}
