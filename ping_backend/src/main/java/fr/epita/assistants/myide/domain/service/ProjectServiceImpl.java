package fr.epita.assistants.myide.domain.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import fr.epita.assistants.myide.domain.entity.Feature;
import fr.epita.assistants.myide.domain.entity.Node;
import fr.epita.assistants.myide.domain.entity.NodeImpl;
import fr.epita.assistants.myide.domain.entity.Project;
import fr.epita.assistants.myide.domain.entity.ProjectImpl;
import fr.epita.assistants.myide.utils.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProjectServiceImpl implements ProjectService {
    private NodeService nodeService;
    public Project project;

    public ProjectServiceImpl(NodeService nodeService) {
        this.nodeService = nodeService;
        this.project = null;
    }

    @Override
    public @NotNull Project load(@NotNull Path root) {
        Node.Type type = Node.Types.FILE;
        if(Files.isDirectory(root)){
            type = Node.Types.FOLDER;
            Logger.log("Open Project Good "+ root.toString());
        }
        else{
            Logger.log("Open File Good "+ root.toString());
        }
        Node rootNode = new NodeImpl(root.toString(), type);
        this.project = new ProjectImpl(rootNode);
        return project;
    }


    @Override
    public @NotNull Feature.ExecutionReport execute(@NotNull Project project, @NotNull Feature.Type featureType, Object... params) {
        Optional<Feature> feature = project.getFeature(featureType);
        if (feature.isPresent()) {
            return feature.get().execute(project, params);
        } else {
            // Handle the case when the feature is not found
            return new Feature.ExecutionReport() {
                @Override
                public boolean isSuccess() {
                    return false;
                }
            };
        }
    }

    @Override
    public @NotNull NodeService getNodeService() {
        return nodeService;
    }
}