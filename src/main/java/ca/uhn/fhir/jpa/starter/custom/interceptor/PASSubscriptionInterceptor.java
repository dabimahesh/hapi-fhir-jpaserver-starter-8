package ca.uhn.fhir.jpa.starter.custom.interceptor;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Subscription;
import org.springframework.stereotype.Component;

@Interceptor
@Component
public class PASSubscriptionInterceptor {

	private static final String PAS_TOPIC =
		"http://hl7.org/fhir/us/davinci-pas/SubscriptionTopic/PASSubscriptionTopic";

	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
	public void beforeCreate(IBaseResource resource) {

		if (!(resource instanceof Subscription subscription)) {
			return;
		}

		if (subscription.getCriteria() == null) {
			return;
		}

		if (PAS_TOPIC.equals(subscription.getCriteria())) {

			// Optional: preserve original PAS topic for debugging
			subscription.getMeta().addTag(
				"custom",
				"original-pas-criteria",
				PAS_TOPIC
			);

			// Convert PAS topic URL → HAPI-compatible criteria
			subscription.setCriteria("Patient?");
		}
	}

	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
	public void beforeUpdate(Subscription subscription) {

		if (subscription == null || subscription.getCriteria() == null) {
			return;
		}

		if (PAS_TOPIC.equals(subscription.getCriteria())) {
			subscription.setCriteria("Patient?");
		}
	}
}