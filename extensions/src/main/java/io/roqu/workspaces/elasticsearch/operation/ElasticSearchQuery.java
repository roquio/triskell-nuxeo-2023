package io.roqu.workspaces.elasticsearch.operation;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.jaxrs.io.documents.PaginableDocumentModelListImpl;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderDefinition;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.elasticsearch.provider.ElasticSearchNxqlPageProvider;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ElasticSearch query operation.
 *
 * @author Cédric Krommenhoek
 */
@Operation(id = ElasticSearchQuery.ID, category = Constants.CAT_FETCH, label = "ElasticSearch query", description = "Perform a query on ElasticSearch, instead of on the repository.", aliases = {"Document.ElasticSearchQuery", "Document.QueryES"})
@NoArgsConstructor
public class ElasticSearchQuery {

    /**
     * Operation identifier.
     */
    public static final String ID = "Repository.ElasticSearchQuery";

    /**
     * Page provider name.
     */
    private static final String PAGE_PROVIDER_NAME = "REST_API_SEARCH_ADAPTER";

    /**
     * Default page size.
     */
    private static final long DEFAULT_PAGE_SIZE = 50L;

    /**
     * Default max results.
     */
    private static final long DEFAULT_MAX_RESULTS = 1000L;


    /**
     * Session.
     */
    @Context
    protected CoreSession session;

    /**
     * Search on all repositories indicator.
     */
    @Param(name = "searchOnAllRepositories", description = "Search on all repositories indicator", required = false)
    private Boolean searchOnAllRepositories;

    /**
     * Query.
     */
    @Param(name = "query", description = "The query to perform.")
    protected String query;

    /**
     * Page size.
     */
    @Param(name = "pageSize", description = "Page size", required = false)
    private Long pageSize;

    /**
     * Current page index.
     */
    @Param(name = "currentPageIndex", description = "Current page index", required = false)
    private Long currentPageIndex;

    /**
     * Max results.
     */
    @Param(name = "maxResults", description = "Max results; useful to avoid slowing down queries", required = false)
    private Long maxResults;

    /**
     * Sort by.
     */
    @Param(name = "sortBy", description = "Sort by", required = false)
    private String sortBy;

    /**
     * Sort order.
     */
    @Param(name = "sortOrder", description = "Sort order", required = false)
    private String sortOrder;

    /**
     * Query parameters.
     */
    @Param(name = "queryParams", description = "Query parameters", required = false)
    private Object[] queryParams;


    /**
     * Run operation.
     *
     * @return operation result
     */
    @SuppressWarnings("unchecked")
    @OperationMethod
    public Object run() throws OperationException {
        // Page provider service
        PageProviderService pageProviderService = Framework.getService(PageProviderService.class);

        // Page provider definition
        PageProviderDefinition pageProviderDefinition = pageProviderService.getPageProviderDefinition(PAGE_PROVIDER_NAME);
        pageProviderDefinition.setPattern(this.query);
        if (this.maxResults == null) {
            pageProviderDefinition.getProperties().put("maxResults", String.valueOf(DEFAULT_MAX_RESULTS));
        } else if (this.maxResults > 0) {
            pageProviderDefinition.getProperties().put("maxResults", String.valueOf(this.maxResults));
        }

        // Page provider properties
        Map<String, Serializable> properties = new HashMap<>();
        properties.put(CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY, (Serializable) this.session);
        if (BooleanUtils.toBoolean(this.searchOnAllRepositories)) {
            properties.put(ElasticSearchNxqlPageProvider.SEARCH_ON_ALL_REPOSITORIES_PROPERTY, String.valueOf(true));
        }

        // Page provider
        List<SortInfo> sortInfos = this.getSortInfos();
        long pageSize = ObjectUtils.defaultIfNull(this.pageSize, DEFAULT_PAGE_SIZE);
        long currentPageIndex = ObjectUtils.defaultIfNull(this.currentPageIndex, 0L);
        PageProvider<DocumentModel> pageProvider = (PageProvider<DocumentModel>) pageProviderService.getPageProvider(PAGE_PROVIDER_NAME, pageProviderDefinition, null, sortInfos, pageSize, currentPageIndex, properties, this.queryParams);

        return new PaginableDocumentModelListImpl(pageProvider);
    }


    /**
     * Get sort infos.
     *
     * @return sort infos
     */
    private List<SortInfo> getSortInfos() {
        // Sort Info Management
        List<SortInfo> sortInfos;
        if (StringUtils.isBlank(this.sortBy)) {
            sortInfos = null;
        } else {
            sortInfos = new ArrayList<>();
            String[] sorts = StringUtils.split(this.sortBy, ",");
            String[] orders = null;
            if (StringUtils.isNotBlank(this.sortOrder)) {
                orders = StringUtils.split(this.sortOrder, ",");
            }

            for (int i = 0; i < sorts.length; i++) {
                String sort = sorts[i];
                boolean sortAscending = StringUtils.equalsIgnoreCase("asc", ArrayUtils.get(orders, i));
                SortInfo sortInfo = new SortInfo(sort, sortAscending);

                sortInfos.add(sortInfo);
            }
        }

        return sortInfos;
    }

}
