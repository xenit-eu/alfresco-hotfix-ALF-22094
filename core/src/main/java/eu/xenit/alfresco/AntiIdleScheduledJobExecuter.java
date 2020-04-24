package eu.xenit.alfresco;

import static org.alfresco.service.namespace.QName.createQName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class AntiIdleScheduledJobExecuter {
    private static final Logger LOG = LoggerFactory.getLogger(AntiIdleScheduledJobExecuter.class);
    private static final String defaultLocation = "/Company Home/Data Dictionary/Anti Idle/Dummy Document";
    private String[] folder = new String[]{"Data Dictionary", "Anti Idle"};
    private String fileName = "Dummy Document";
    private boolean enabled = true;
    private NodeService nodeService;
    private NodeLocatorService nodeLocatorService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private NodeRef nodeRef;

    /**
     * Public API access
     */
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.nodeLocatorService = serviceRegistry.getNodeLocatorService();
        this.retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();
    }

    public void setFileLocation(String location) {
        if (StringUtils.isEmpty(location)) {
            LOG.debug("Custom location not set, using default " + defaultLocation);
            return;
        }
        List<String> segments = Arrays.stream(location.split("/"))
                .filter(s -> !StringUtils.isEmpty(s))
                .collect(Collectors.toList());
        if (segments.size() < 2) {
            LOG.debug("Custom location should have at least 2 segments, using default " + defaultLocation);
            return;
        }
        if (!"Company Home".equals(segments.remove(0))) {
            LOG.debug("Only dummy files in Company Home tree supported for now, using default " + defaultLocation);
            return;
        }
        LOG.debug("Setting location to " + location);
        fileName = segments.remove(segments.size() - 1);
        folder = segments.toArray(new String[0]);
    }

    public void setEnabled(boolean enabled) {
        LOG.debug("setting enabled to " + enabled);
        this.enabled = enabled;
    }

    /**
     * Executer implementation
     */
    public void execute() {
        if (!enabled) {
            LOG.debug("Not running " + this.getClass().getCanonicalName() + ", not enabled");
            return;
        }
        LOG.debug("Running " + this.getClass().getCanonicalName());
        retryingTransactionHelper.doInTransaction(() -> {
            if (nodeRef == null) {
                nodeRef = getOrCreateDummyFile();
            }
            LOG.debug("Updating " + nodeRef);
            updateDummyDoc(nodeRef);
            return null;
        });
    }

    private NodeRef getOrCreateDummyFile() {
        NodeRef existing = fileAlreadyExists();
        if (existing != null) {
            LOG.debug("Dummy already exists: " + existing);
            return existing;
        }
        nodeRef = createDummyDoc();
        LOG.debug("Created dummy" + nodeRef);
        return nodeRef;
    }

    private NodeRef fileAlreadyExists() {
        NodeRef parent = getFolderByDisplayPath(folder, true);
        return nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, fileName);
    }

    private NodeRef createDummyDoc() {
        NodeRef parent = getFolderByDisplayPath(folder, true);
        ChildAssociationRef node = nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                createQName(
                        NamespaceService.CONTENT_MODEL_1_0_URI,
                        fileName),
                ContentModel.PROP_CONTENT,
                new HashMap<QName, Serializable>() {{
                    put(ContentModel.PROP_NAME, fileName);
                }});
        return node.getChildRef();
    }

    private void updateDummyDoc(NodeRef nodeRef) {
        nodeService.setProperty(
                nodeRef,
                ContentModel.PROP_DESCRIPTION,
                "I was updated to create a dummy transaction on " + LocalDateTime.now());
    }

    public NodeRef getFolderByDisplayPath(final String[] path, final boolean create) {
        NodeRef cursor = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);

        for (String folder : path) {
            NodeRef child = nodeService.getChildByName(cursor, ContentModel.ASSOC_CONTAINS, folder);
            if (child != null) {
                cursor = child;
            } else if (create) {
                Map<QName, Serializable> folderProp = new HashMap<>();
                folderProp.put(ContentModel.PROP_NAME, folder);

                cursor = nodeService.createNode(cursor,
                        ContentModel.ASSOC_CONTAINS,
                        createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folder),
                        ContentModel.TYPE_FOLDER,
                        folderProp).getChildRef();
            } else {
                return null;
            }
        }

        return cursor;
    }


}
