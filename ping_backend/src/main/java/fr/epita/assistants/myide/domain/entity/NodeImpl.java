package fr.epita.assistants.myide.domain.entity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;


public class NodeImpl implements Node {
    private final Path path;
    private final Type type;
    private final List<Node> children;

    public NodeImpl(String path, Type type) {
        this.path = Paths.get(path);
        this.type = type;
        if (type == Node.Types.FOLDER) {
            this.children = populateChildren();
        } else {
            this.children = new ArrayList<>();
        }
    }

    public List<Node> populateChildren() {
        List<Node> childrenList = new ArrayList<>();
        try {
            Files.list(path)
                    .map(childPath -> new NodeImpl(childPath.toString(), determineType(childPath)))
                    .forEach(childrenList::add);
        } catch (Exception e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
        return childrenList;
    }
    private Type determineType(Path childPath) {
        return Files.isDirectory(childPath) ? Node.Types.FOLDER : Node.Types.FILE;
    }

    @Override
    public @NotNull Path getPath() {
        return path;
    }

    @Override
    public @NotNull Type getType() {
        return type;
    }

    @Override
    public @NotNull List<@NotNull Node> getChildren() {
        return children;
    }
}