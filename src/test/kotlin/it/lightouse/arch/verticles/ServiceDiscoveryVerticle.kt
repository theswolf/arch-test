package it.lightouse.arch.verticles

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.json.JsonObject
import io.vertx.servicediscovery.Record
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions
import io.vertx.servicediscovery.types.HttpEndpoint


fun main(args: Array<String>) {
	//var sd: ServiceDiscoveryVerticle = ServiceDiscoveryVerticle();
	var vertx = Vertx.vertx();
	vertx.deployVerticle(ServiceDiscoveryVerticle::class.qualifiedName);
	println("Hello, world!")
}

class ServiceDiscoveryVerticle : AbstractVerticle {

	constructor() : super();


	override fun start() {

		val serviceDiscovery: ServiceDiscovery = ServiceDiscovery.create(vertx,
				ServiceDiscoveryOptions()
						.setAnnounceAddress("service-announce")
						.setName("my-name"));
		val record1 = Record()
				.setType("eventbus-service-proxy")
				.setLocation(JsonObject().put("endpoint", "the-service-address"))
				.setName("my-service")
				.setMetadata(JsonObject().put("some-label", "some-value"));



		serviceDiscovery.publish(record1, Handler<AsyncResult<Record>> { ar ->
			if (ar.succeeded()) {
				System.out.println("\"" + record1.getName() + "\" successfully published!");
				var publishedRecord = ar.result();
			} else {
				// publication failed
			}
		});

		// create a record from type
		val record2 = HttpEndpoint.createRecord("some-rest-api", "localhost", 8080, "/api");


		//publish the service
		serviceDiscovery.publish(record2, Handler<AsyncResult<Record>> { ar ->
			if (ar.succeeded()) {
				System.out.println("\"" + record2.getName() + "\" successfully published!");
				val publishedRecord = ar.result();
			} else {
				// publication failed
			}
		});

		//unpublish "my-service"
		serviceDiscovery.unpublish(record1.getRegistration(), Handler<AsyncResult<Void>> { ar ->
			if (ar.succeeded()) {
				System.out.println("\"" + record1.getName() + "\" successfully unpublished");
			} else {
				// cannot un-publish the service, may have already been removed, or the record is not published
			}
		});

		serviceDiscovery.getRecord(java.util.function.Function<Record, Boolean> {
			r ->
			r.getName().equals(record2.getName())
		},
				Handler<AsyncResult<Record>> {
					ar ->
					if (ar.succeeded()) {
						if (ar.result() != null) {
							// Retrieve the service reference
							val reference = serviceDiscovery.getReference(ar.result());
							// Retrieve the service object
							val client: HttpClient = reference.get();
							System.out.println("Consuming \"" + record2.getName() + "\"");

							client.getNow("/api", Handler<HttpClientResponse> { response ->
								//release the service
								reference.release();

							});
						}
					}
				});


	}

}

