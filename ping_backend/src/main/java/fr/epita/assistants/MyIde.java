package fr.epita.assistants;

import java.nio.file.Path;

import fr.epita.assistants.myide.domain.service.NodeService;
import fr.epita.assistants.myide.domain.service.NodeServiceImpl;
import fr.epita.assistants.myide.domain.service.ProjectService;
import fr.epita.assistants.myide.domain.service.ProjectServiceImpl;
import fr.epita.assistants.myide.utils.Given;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Starter class, we will use this class and the init method to get a
 * configured instance of {@link ProjectService}.
 */
@ApplicationScoped
@Given(overwritten = false)
public class MyIde {

    /**
     * Init method. It must return a fully functional implementation of {@link ProjectService}.
     *
     * @return An implementation of {@link ProjectService}.
     */
    public static ProjectService init(final Configuration configuration) {
        NodeService nodeService = new NodeServiceImpl();
        return new ProjectServiceImpl(nodeService);
    }

    /**
     * Record to specify where the configuration of your IDE
     * must be stored. Might be useful for the search feature.
     */
    public record Configuration(Path indexFile,
                                Path tempFolder) {
    }

}
