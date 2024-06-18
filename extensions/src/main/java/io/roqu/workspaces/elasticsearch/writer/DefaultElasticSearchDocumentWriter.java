package io.roqu.workspaces.elasticsearch.writer;

import com.fasterxml.jackson.core.JsonGenerator;
import io.roqu.workspaces.elasticsearch.registry.ElasticSearchDocumentWriter;
import lombok.RequiredArgsConstructor;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.IOException;
import java.util.Map;

/**
 * Default ElasticSearch document writer.
 *
 * @author Cédric Krommenhoek
 * @see ElasticSearchDocumentWriter
 */
@RequiredArgsConstructor
public class DefaultElasticSearchDocumentWriter implements ElasticSearchDocumentWriter {

    @Override
    public boolean accept(DocumentModel document) {
        return true;
    }


    @Override
    public void write(JsonGenerator jsonGenerator, DocumentModel document, String[] schemas, Map<String, String> contextParameters) throws IOException {
        jsonGenerator.writeBooleanField("nav:folder", document.isFolder());
    }

}
