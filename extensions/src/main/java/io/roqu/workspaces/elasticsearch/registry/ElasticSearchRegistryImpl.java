package io.roqu.workspaces.elasticsearch.registry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * ElasticSearch registry implementation.
 *
 * @author Cédric Krommenhoek
 * @see DefaultComponent
 * @see ElasticSearchRegistry
 */
@RequiredArgsConstructor
@Getter
public class ElasticSearchRegistryImpl extends DefaultComponent implements ElasticSearchRegistry {

    /**
     * ElasticSearch writer extension point.
     */
    public static final String WRITER_EXTENSION_POINT = "writer";


    /**
     * Registered ElasticSearch writers.
     */
    private final List<ElasticSearchDocumentWriter> writers = new ArrayList<>();


    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance component) {
        if (extensionPoint.equals(WRITER_EXTENSION_POINT) && (contribution instanceof ElasticSearchDocumentWriterDescriptor descriptor)) {
            String className = descriptor.getClassName();
            ElasticSearchDocumentWriter writer;
            try {
                Object object = Class.forName(className).getDeclaredConstructor().newInstance();
                writer = (ElasticSearchDocumentWriter) object;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            this.writers.add(writer);
        } else {
            throw new IllegalStateException(String.format("Invalid extension point: %s", extensionPoint));
        }
    }


    @Override
    public List<ElasticSearchDocumentWriter> getAcceptedWriters(DocumentModel document) {
        return this.writers.stream().filter(writer -> writer.accept(document)).toList();
    }

}
