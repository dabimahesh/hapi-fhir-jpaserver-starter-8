package ca.uhn.fhir.jpa.starter.custom.miscellaneous;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.stereotype.Component;

//@Component // Registers automatically with HAPI FHIR 8
public class ClaimInquireOperationProvider implements IResourceProvider {

	@Override
	public Class<Claim> getResourceType() {
		return Claim.class;
	}

	/**
	 * Handles: POST [base_url]/Claim/$inquire
	 */
	@Operation(name = "$inquire", type = Claim.class, idempotent = true)
	public Parameters claimInquire(@OperationParam(name = "resource") Bundle requestBundle) {

		// 1. Unpack the bundle to find the inquiry criteria (e.g., Claim ID, PreAuthRef, or Patient)
		Claim inquiryCriteria = null;
		for (Bundle.BundleEntryComponent entry : requestBundle.getEntry()) {
			if (entry.getResource() instanceof Claim) {
				inquiryCriteria = (Claim) entry.getResource();
				break;
			}
		}

		// 2. Query your internal database for the matching Claim/Prior Authorization status
		// (Example: Look up using inquiryCriteria.getIdentifier() or internal reference)

		// 3. Build the outbound Parameters payload
		Parameters outerParameters = new Parameters();

		// Create an inner response bundle containing the current status
		Bundle matchBundle = new Bundle();
		matchBundle.setType(Bundle.BundleType.COLLECTION);

		ClaimResponse currentStatusResponse = new ClaimResponse();
		currentStatusResponse.setStatus(ClaimResponse.ClaimResponseStatus.ACTIVE);
		currentStatusResponse.setOutcome(ClaimResponse.RemittanceOutcome.COMPLETE); // or QUEUED
		currentStatusResponse.setDisposition("Authorization is approved and active.");

		matchBundle.addEntry().setResource(currentStatusResponse);

		// Add the bundle into the Parameters wrapper using the required "return" parameter name
		outerParameters.addParameter()
			.setName("return")
			.setResource(matchBundle);

		return outerParameters;
	}
}