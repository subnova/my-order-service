package utils.jsonschema;

import com.github.fge.jsonschema.core.report.ProcessingReport;

public class JsonSchemaConstraintViolationException extends RuntimeException {
    private ProcessingReport processingMessages;

    public JsonSchemaConstraintViolationException(ProcessingReport processingMessages) {
        this.processingMessages = processingMessages;
    }

    public ProcessingReport getProcessingMessages() {
        return processingMessages;
    }
}
