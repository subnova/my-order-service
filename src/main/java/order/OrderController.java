package order;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import utils.jsonschema.JsonSchema;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
public class OrderController {
    private final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private Source messageSource;

    @PostMapping("/orders")
    public ResponseEntity<Map<Object, Object>> createOrder(@JsonSchema("/order.json") @RequestBody Map<String, Object> order) throws ExecutionException, InterruptedException {
        String orderId = generateOrderId();

        logger.info("Sending order {} with id {} to Kafka", order, orderId);

        order.put("orderId", orderId);

        messageSource.output().send(
            MessageBuilder
                .withPayload(order)
                .build());

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(orderId).toUri();

        return ResponseEntity.created(uri).body(ImmutableMap.of());
    }

    private String generateOrderId() {
        return UUID.randomUUID().toString();
    }
}
