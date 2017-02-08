package utils.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestControllerAdvice
// TODO: improve error reporting (grab error and location fields, simplify location and format with error)
// TODO: add support for loading common schemas
// TODO: better handling of badly formed JSON (and other processing exceptions)
public class JsonSchemaRequestBodyAdvice extends RequestBodyAdviceAdapter {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasParameterAnnotation(JsonSchema.class) && AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        JsonSchema schemaAnnotation = parameter.getParameterAnnotation(JsonSchema.class);

        byte[] bytes = StreamUtils.copyToByteArray(inputMessage.getBody());

        InputStream jsonSchemaInputStream = new ClassPathResource(schemaAnnotation.value()).getInputStream();
        JsonNode jsonSchemaNode = objectMapper.readTree(jsonSchemaInputStream);
        try {
            com.github.fge.jsonschema.main.JsonSchema schema = JsonSchemaFactory.byDefault().getJsonSchema(jsonSchemaNode);
                JsonNode valueNode = objectMapper.readTree(bytes);
            ProcessingReport processingMessages = schema.validateUnchecked(valueNode);
            if (!processingMessages.isSuccess()) {
                throw new JsonSchemaConstraintViolationException(processingMessages);
            }
        } catch (ProcessingException e) {
            throw new RuntimeException(e);
        }

        return new HttpInputMessage() {
            @Override
            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }

    @ExceptionHandler(value = {JsonSchemaConstraintViolationException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public List<String> handleJsonSchemaConstraintViolationException(JsonSchemaConstraintViolationException e) {
        return StreamSupport.stream(e.getProcessingMessages().spliterator(), false)
            .map(ProcessingMessage::toString)
            .collect(Collectors.toList());
    }
}
