package io.roqu.workspaces.elasticsearch.registry;

import lombok.Data;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * ElasticSearch document writer descriptor.
 *
 * @author Cédric Krommenhoek
 */
@XObject("writer")
@Data
public class ElasticSearchDocumentWriterDescriptor {

    /**
     * Class name.
     */
    @XNode("@class")
    private String className;

}
