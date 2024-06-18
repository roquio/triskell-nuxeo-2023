package io.roqu.workspaces.elasticsearch.registry;

import com.fasterxml.jackson.core.JsonGenerator;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.IOException;
import java.util.Map;

/**
 * ElasticSearch document writer interface.
 *
 * @author Cédric Krommenhoek
 */
public interface ElasticSearchDocumentWriter {

    /**
     * Accept document.
     *
     * @param document document
     * @return true if this document is accepted
     */
    boolean accept(DocumentModel document);


    /**
     * Write.
     *
     * @param jsonGenerator     JSON generator
     * @param document          document
     * @param schemas           schemas
     * @param contextParameters context parameters
     */
    void write(JsonGenerator jsonGenerator, DocumentModel document, String[] schemas, Map<String, String> contextParameters) throws IOException;

}
