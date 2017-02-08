package order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@Category(UnitTest.class)
public class OrderSchemaTest {
    @Test
    public void shouldBeAbleToParseOrderSchema() throws IOException {
        InputStream jsonSchemaInputStream = new ClassPathResource("/order.json").getInputStream();
        JsonNode jsonSchemaNode = new ObjectMapper().readTree(jsonSchemaInputStream);
        assertThat(jsonSchemaNode).isNotNull();
    }
}
