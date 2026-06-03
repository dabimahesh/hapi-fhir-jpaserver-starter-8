package ca.uhn.fhir.jpa.starter.custom.miscellaneous;

import ca.uhn.fhir.jpa.starter.custom.helper.HapiPropertiesConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WellKnownFilter implements Filter {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String STRING_SPLIT_PARAM = ",";

	@Override
	public void doFilter(
		ServletRequest request,
		ServletResponse response,
		FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		String uri = req.getRequestURI();

		// Handle SMART Configuration
		if (uri.endsWith("/.well-known/smart-configuration")) {

			HapiPropertiesConfig hapiConfig = new HapiPropertiesConfig();

			Map<String, Object> smartConfig = Map.ofEntries(
				Map.entry(
					"token_endpoint_auth_signing_alg_values_supported",
					hapiConfig.getToken_endpoint_auth_signing_alg_values_supported().split(STRING_SPLIT_PARAM)
				),
				Map.entry(
					"capabilities",
					hapiConfig.getsmart_capabilities().split(STRING_SPLIT_PARAM)
				),
				Map.entry(
					"code_challenge_methods_supported",
					hapiConfig.getCode_challenge_methods_supported().split(STRING_SPLIT_PARAM)
				),
				Map.entry(
					"introspection_endpoint",
					hapiConfig.getintrospection_endpoint()
				),
				Map.entry(
					"grant_types_supported",
					hapiConfig.getgrant_types_supported().split(STRING_SPLIT_PARAM)
				),
				Map.entry(
					"jwks_uri",
					hapiConfig.getjwks_uri()
				),
				Map.entry(
					"revocation_endpoint",
					hapiConfig.getrevocation_endpoint()
				),
				Map.entry(
					"token_endpoint_auth_methods_supported",
					hapiConfig.getsmart_token_endpoint_auth_methods_supported().split(STRING_SPLIT_PARAM)
				),
				Map.entry(
					"issuer",
					hapiConfig.getissuer()
				),
				Map.entry(
					"authorization_endpoint",
					hapiConfig.getAuthorization_endpoint()
				),
				Map.entry(
					"token_endpoint",
					hapiConfig.gettoken_endpoint()
				)
			);

			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("application/json");
			OBJECT_MAPPER.writeValue(resp.getOutputStream(), smartConfig);
			return;
		}

		// Handle OpenID Configuration
		if (uri.endsWith("/.well-known/openid-configuration")) {

			HapiPropertiesConfig hapiConfig = new HapiPropertiesConfig();

			Map<String, Object> openIdConfig = Map.of(
				"issuer", hapiConfig.getissuer(),
				"authorization_endpoint", hapiConfig.getAuthorization_endpoint(),
				"token_endpoint", hapiConfig.gettoken_endpoint(),
				"userinfo_endpoint", hapiConfig.getuserinfo_endpoint(),
				"jwks_uri", hapiConfig.getjwks_uri()
			);

			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("application/json");
			OBJECT_MAPPER.writeValue(resp.getOutputStream(), openIdConfig);
			return;
		}

		// Let HAPI handle all other requests
		chain.doFilter(request, response);
	}
}