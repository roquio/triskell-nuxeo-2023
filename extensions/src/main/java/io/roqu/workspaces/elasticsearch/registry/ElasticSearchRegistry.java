package io.roqu.workspaces.elasticsearch.registry;

import org.nuxeo.ecm.core.api.DocumentModel;

import java.util.List;

/**
 * ElasticSearch registry interface.
 *
 * @author Cédric Krommenhoek
 */
public interface ElasticSearchRegistry {

    /**
     * Get accepted ElasticSearch document writers.
     *
     * @param document document
     * @return ElasticSearch document writers
     */
    List<ElasticSearchDocumentWriter> getAcceptedWriters(DocumentModel document);

}
