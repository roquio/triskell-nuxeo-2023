package io.roqu.workspaces.elasticsearch.registry;

import com.fasterxml.jackson.core.JsonGenerator;
import lombok.RequiredArgsConstructor;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.elasticsearch.io.JsonESDocumentWriter;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Customized JSON ElasticSearch document writer.
 *
 * @author Cédric Krommenhoek
 * @see JsonESDocumentWriter
 */
@RequiredArgsConstructor
public class CustomizedJsonElasticSearchDocumentWriter extends JsonESDocumentWriter {

    @Override
    public void writeESDocument(JsonGenerator jsonGenerator, DocumentModel document, String[] schemas, Map<String, String> contextParameters) throws IOException {
        jsonGenerator.writeStartObject();
        super.writeSystemProperties(jsonGenerator, document);
        super.writeSchemas(jsonGenerator, document, schemas);
        super.writeContextParameters(jsonGenerator, document, contextParameters);
        this.writeExtraProperties(jsonGenerator, document, schemas, contextParameters);
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
    }


    /**
     * Write extra-properties.
     *
     * @param jsonGenerator     JSON generator
     * @param document          document
     * @param schemas           schemas
     * @param contextParameters context parameters
     */
    private void writeExtraProperties(JsonGenerator jsonGenerator, DocumentModel document, String[] schemas, Map<String, String> contextParameters) throws IOException {
        // ElasticSearch registry
        ElasticSearchRegistry registry = Framework.getService(ElasticSearchRegistry.class);

        List<ElasticSearchDocumentWriter> writers = registry.getAcceptedWriters(document);
        for (ElasticSearchDocumentWriter writer : writers) {
            writer.write(jsonGenerator, document, schemas, contextParameters);
        }
    }

}
