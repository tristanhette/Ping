package fr.epita.assistants.myide.domain.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import fr.epita.assistants.myide.domain.entity.Node;
import fr.epita.assistants.myide.domain.entity.NodeImpl;
import fr.epita.assistants.myide.utils.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NodeServiceImpl implements NodeService {

    @Override
    public Node update(Node node, int from, int to, byte[] insertedContent) {
        Path path = node.getPath();
        byte[] fileContent;
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            Logger.logError("File does not exist or is not a file");
            return null;
        }
        try {
            fileContent = Files.readAllBytes(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            fileContent = new byte[0];
            e.printStackTrace();

        }

        if (from < 0 || from > fileContent.length || to < 0 || to > fileContent.length || from > to) {
            Logger.logError("Invalid range");
            return null;
        }


        byte[] newContent = new byte[fileContent.length - (to - from) + insertedContent.length];

        System.arraycopy(fileContent, 0, newContent, 0, from);
        System.arraycopy(insertedContent, 0, newContent, from, insertedContent.length);
        System.arraycopy(fileContent, to, newContent, from + insertedContent.length, fileContent.length - to);

        try {
            Files.write(path, newContent);
        } catch (IOException e) {
            Logger.logError("Failed to write to file: " + e.getMessage());

        }
        return node;
    }

    @Override
    public boolean delete(Node node) {
        try {
            Path path = node.getPath();

            if (Files.isDirectory(path)) {
                Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                        }
                    });
            } else {
                Files.delete(path);
            }
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public Node create(Node folder,String name, Node.Type type) {

        Path folderPath = folder != null ? folder.getPath() : Paths.get("");
        Path newPath = folderPath.resolve(name);
        
        if(Files.exists(newPath))
        {
            Logger.logError("Same Name File Not Created " + name);
            return null;
        }
        else if (type.equals(Node.Types.FILE)) {
            try {
                Files.createFile(newPath);
                Logger.log("Create File Work " + name);
            } catch (IOException e) {
                Logger.logError("Create File Not Working " + name);
                return null;
            }
        } else if (type.equals(Node.Types.FOLDER)) {
            try {
                Files.createDirectory(newPath);
                Logger.log("Create Folder Work " + name);
            } catch (IOException e) {
                Logger.logError("Create File Not Working " + name);
                return null;
            }
        } else {
                Logger.logError("Create File/Folder Not Working " + name);
                return null;
        }
        return new NodeImpl(newPath.toString(), type);
    }

    @Override
    public Node move(Node nodeToMove, Node destinationFolder) {
        Path sourcePath = nodeToMove.getPath();
        Path destinationPath = destinationFolder.getPath().resolve(sourcePath.getFileName());

        try {
            Files.move(sourcePath, destinationPath);
        } catch (IOException e) {
            Logger.logError("Move Not Working " + nodeToMove.getPath().toString() + " to " + destinationFolder.getPath().toString());
            return null;
        }
        return new NodeImpl(destinationPath.toString(), nodeToMove.getType());
    }
}