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
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class AntiIdleScheduledJobExecuter {
    private static final Logger LOG = LoggerFactory.getLogger(AntiIdleScheduledJobExecuter.class);
    private static final String defaultLocation = "/Anti Idle/Dummy Document";
    private String[] folder;
    private String fileName;
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
            setFolderAndFileName(toSegments(defaultLocation));
            return;
        }
        List<String> segments = toSegments(location);
        if (segments.isEmpty()) {
            LOG.debug("Custom location should have at least one segment, using default " + defaultLocation);
            setFolderAndFileName(toSegments(defaultLocation));
            return;
        }
        LOG.debug("Setting location to " + location);
        setFolderAndFileName(segments);
    }

    private void setFolderAndFileName(List<String> segments) {
        fileName = segments.get(segments.size() - 1);
        folder = segments.subList(0, segments.size() - 1).toArray(new String[0]);
    }

    private List<String> toSegments(String location) {
        return Arrays.stream(location.split("/"))
                .filter(s -> !StringUtils.isEmpty(s))
                .collect(Collectors.toList());
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
        NodeRef parent = getFolderByDisplayPath(folder, true);
        NodeRef existing = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, fileName);;
        if (existing != null) {
            LOG.debug("Dummy already exists: " + existing);
            return existing;
        }
        nodeRef = createDummyDoc(parent);
        LOG.debug("Created dummy" + nodeRef);
        return nodeRef;
    }

    private NodeRef createDummyDoc(NodeRef parent) {
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
        NodeRef cursor = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        boolean storeRootPassed = false;
        for (String folder : path) {
            NodeRef child = find(cursor, folder);
            if (child != null) {
                cursor = child;
                storeRootPassed = true;
            } else if (create) {
                cursor = create(cursor, folder, storeRootPassed);
                storeRootPassed = true;
            } else {
                return null;
            }
        }

        return cursor;
    }

    private NodeRef find(NodeRef parent, String folder) {
        return nodeService.getChildAssocs(parent).stream()
                .filter(ref -> ref.getQName().getLocalName().equals(folder))
                .map(ChildAssociationRef::getChildRef)
                .findFirst().orElse(null);
    }

    private NodeRef create(NodeRef parent, String folder, boolean storeRootPassed) {
        Map<QName, Serializable> folderProp = new HashMap<>();
        folderProp.put(ContentModel.PROP_NAME, folder);
        return nodeService.createNode(parent,
                storeRootPassed ? ContentModel.ASSOC_CONTAINS : ContentModel.ASSOC_CHILDREN,
                createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folder),
                ContentModel.TYPE_FOLDER,
                folderProp).getChildRef();
    }

}
