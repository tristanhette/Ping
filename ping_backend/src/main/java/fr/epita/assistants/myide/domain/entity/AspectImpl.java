package fr.epita.assistants.myide.domain.entity;

import javax.validation.constraints.NotNull;
import java.util.*;

public class AspectImpl implements Aspect {
    Type type;
    List<Feature> list;

    public AspectImpl(Type type) {
        this.type = type;
        List<Feature> listt = new ArrayList<>();
        switch (type)
        {
            case ANY -> {
                for (Mandatory.Features.Any type1 : Mandatory.Features.Any.values()) {
                    listt.add(new FeatureImpl(type1));
                }
            }
            case MAVEN -> {
                for (Mandatory.Features.Maven type1 : Mandatory.Features.Maven.values()) {
                    listt.add( new FeatureImpl(type1));
                }
            }
            case GIT -> {

                for (Mandatory.Features.Git type1 : Mandatory.Features.Git.values()) {
                    listt.add( new FeatureImpl(type1));
                }
            }
        }
        this.list = listt;
    }

    public @NotNull List<Feature> getFeatureList()
    {
        return list;
    }

    @Override
    public Type getType() {
        return type;
    }


    public enum Type implements Aspect.Type {
        ANY,
        MAVEN,
        GIT
    }

}
