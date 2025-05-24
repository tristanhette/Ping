package fr.epita.assistants.myide.domain.entity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.validation.constraints.NotNull;

import fr.epita.assistants.myide.utils.Logger;

public class ProjectImpl implements Project {
    private Node rootNode;
    private final Set<Aspect> aspects;

    public ProjectImpl(Node rootNode) {
        this.rootNode = rootNode;
        this.aspects = new HashSet<>();
        this.aspects.add(new AspectImpl(AspectImpl.Type.ANY));
        for (Node child : rootNode.getChildren())
        {
            if (child.getPath().getFileName().toString().equals(".git"))
            {
                //Logger.log("GIIIIIIIIT");
                this.aspects.add(new AspectImpl(AspectImpl.Type.GIT));
            }
            if (child.getPath().getFileName().toString().equals("pom.xml"))
            {
                
                //Logger.log("Mavennnn");
                this.aspects.add(new AspectImpl(AspectImpl.Type.MAVEN));
            }
        }
    }

    @Override
    public @NotNull Node getRootNode() {
        return rootNode;
    }

    @Override
    public @NotNull Set<Aspect> getAspects() {
        return aspects;
    }

    @Override
    public @NotNull Optional<Feature> getFeature(@NotNull Feature.Type featureType) {
        return getAspects().stream()
                .flatMap(aspect -> aspect.getFeatureList().stream())
                .filter(feature -> feature.type().equals(featureType))
                .findFirst();
    }
}