package order;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

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

        if (waitForStoredOrder(orderId)) {
            return ResponseEntity.created(uri).body(ImmutableMap.of());
        }
        return ResponseEntity.created(uri).body(ImmutableMap.of("warning", "Order not available yet"));
    }

    private boolean waitForStoredOrder(String orderId) throws InterruptedException {
        for (int i = 0; i < 10; ++i) {
            if (redisTemplate.opsForHash().hasKey(orderId, "orderId")) {
                return true;
            }
            Thread.sleep(50);
        }
        return false;
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Map<Object, Object>> retrieveOrder(@PathVariable("id") String orderId) {
        Map<Object, Object> order = redisTemplate.opsForHash().entries(orderId);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(order);
        }
    }

    private String generateOrderId() {
        return UUID.randomUUID().toString();
    }
}
