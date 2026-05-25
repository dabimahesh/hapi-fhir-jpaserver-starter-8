package ca.uhn.fhir.jpa.starter.custom.miscellaneous;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;

public class ClaimSubmitOperationProvider implements IResourceProvider {

	@Override
	public Class<? extends Resource> getResourceType() {
		return Claim.class;
	}

	/**
	 * Handles POST [base_url]/Claim/$submit
	 */
	@Operation(name = "$submit", type = Claim.class, idempotent = false)
	public Bundle claimSubmit(
		@OperationParam(name = "resource") Bundle requestBundle,
		RequestDetails theRequestDetails) {

		// 1. Process and parse the incoming request Bundle (Extract Claim, Patient, etc.)
		// 2. Route data into your billing or prior-auth adjudication logic

		// 3. Build your response components
		Bundle responseBundle = new Bundle();
		responseBundle.setType(Bundle.BundleType.COLLECTION);

		ClaimResponse claimResponse = new ClaimResponse();
		// Set your adjudication results here...

		responseBundle.addEntry().setResource(claimResponse);

		return responseBundle; // HAPI serializes this automatically into the HTTP response
	}
}