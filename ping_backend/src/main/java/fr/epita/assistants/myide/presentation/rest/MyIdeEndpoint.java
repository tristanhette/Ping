package fr.epita.assistants.myide.presentation.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import fr.epita.assistants.myide.domain.entity.Mandatory;
import fr.epita.assistants.myide.domain.entity.Node;
import fr.epita.assistants.myide.domain.entity.NodeImpl;
import fr.epita.assistants.myide.domain.entity.Project;
import fr.epita.assistants.myide.domain.service.ProjectServiceImpl;
import fr.epita.assistants.myide.utils.Logger;
import io.quarkus.vertx.http.runtime.devmode.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MyIdeEndpoint {

    Project proj = null;
    @Inject
    ProjectServiceImpl projectService;

    
    JsonArray  recTree(JsonArray json,Node node)
    {
        
        if(node.isFile())
        {
            json.add(node.getPath().toString());
        }
        else{
            List<Node> L = node.getChildren();
        
            for( Node Child : L)
            {
                recTree(json, Child);
                
            }
        }

        return json;
    }


    @POST
    @Path("/open/project")
    public Response openProject(RequestEndpoint requestEndpoint) {
            String filename = requestEndpoint.getPath();
            Logger.log("wd: " + System.getProperty("user.dir"));
            java.nio.file.Path proj = java.nio.file.Paths.get(filename);
            if(!Files.exists(proj)){
                Logger.logError("test openProj not work :" + filename );
                return Response.status(Response.Status.NOT_FOUND).entity("file dont exist").build();
            }
            Project projectNode = projectService.load(proj);
            this.proj = projectNode;
            Logger.log("Open/Load Folder " + filename);
            JsonArray tree = recTree(new  JsonArray(),projectNode.getRootNode());
            return Response.ok(tree).build();
    }

    @POST
    @Path("/open/file")
    public Response openFile(RequestEndpoint requestEndpoint) {
        String filename = requestEndpoint.getPath();
        java.nio.file.Path proj = java.nio.file.Paths.get(filename);
        if(!Files.exists(proj)){
            Logger.logError("test openProj not work :" + filename );
            return Response.status(Response.Status.NOT_FOUND).entity("file dont exist").build();
        }
        Project projectNode = projectService.load(proj);
        this.proj = projectNode;
        Logger.log("Open/Load File " + filename);
        return Response.ok(true).build();
    }

    @POST
    @Path("/create/file")
    public Response createFile(RequestEndpoint requestEndpoint) {
        
            String filename = requestEndpoint.getPath();
            
            java.nio.file.Path file = java.nio.file.Paths.get(filename);
            if(Files.exists(file)){
                Logger.logError("test createFile not working :" + filename );
                return Response.status(Response.Status.NOT_FOUND).entity("file already exist").build();
            }
            Logger.log("test createFile :" + filename );

            Node fileNode = projectService.getNodeService().create(
                    proj.getRootNode(),
                    filename,
                    Node.Types.FILE
            );
            if(fileNode == null){
                Logger.logError("test createFile not working :" + filename );
            }
            else{
                java.nio.file.Path getpar = fileNode.getPath().getParent();
                if(getpar == null)
                    getpar = java.nio.file.Paths.get("");
                Node parent = findNodeRecursively(proj.getRootNode(),getpar.toString());
                parent.getChildren().add(fileNode);
                Logger.log("test createFile :" + filename );
            }
            //this.proj = projectService.load(proj.getRootNode().getPath());
            return Response.ok(true).build();
        /* } catch (Exception e) {
            Logger.logError("Failed to create file: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }*/
    }

    @POST
    @Path("/create/folder")
    public Response createFolder(RequestEndpoint requestEndpoint) {
        // Create a Node object with file content

        String filename = requestEndpoint.getPath();
        //if(proj == null)
            //   proj = projectService.load(Paths.get(""));
        java.nio.file.Path file = java.nio.file.Paths.get(filename);
        if(Files.exists(file)){
            Logger.logError("test createFile not working :" + filename );
            return Response.status(Response.Status.NOT_FOUND).entity("file already exist").build();
        }
        Logger.log("test createFolder :" + filename );
        Node fileNode = projectService.getNodeService().create(
                proj.getRootNode(),
                filename,
                Node.Types.FOLDER
        );
        if(fileNode == null){
            Logger.logError("test createFolder not working :" + filename );
        }
        else{
            java.nio.file.Path getpar = fileNode.getPath().getParent();
            if(getpar == null)
                getpar = java.nio.file.Paths.get("");
            Node parent = findNodeRecursively(proj.getRootNode(),getpar.toString());
            parent.getChildren().add(fileNode);
            Logger.log("test createFolder :" + filename );
        }
        //this.proj = projectService.load(proj.getRootNode().getPath());
        return Response.ok(true).build();
    }


    @POST
    @Path("/delete/file")
    public Response deleteFile(RequestEndpoint requestEndpoint) {

        String filename = requestEndpoint.getPath();
        java.nio.file.Path nodePath = java.nio.file.Paths.get(filename);
        if(!Files.exists(nodePath) || Files.isDirectory(nodePath)){
            Logger.logError("test deleteFile :" + filename );
            return Response.status(Response.Status.NOT_FOUND).entity("file dont exist or directory").build();
        }
        Logger.log("test deleteFile :" + filename );
        Node nodeRemove = findNodeRecursively(proj.getRootNode(),filename);
        Boolean fileNode = projectService.getNodeService().delete(nodeRemove);
        if(fileNode == false){
            Logger.logError("test deleteFile not working :" + filename );
            return Response.status(Response.Status.NOT_FOUND).entity("file dont exist").build();
        }
        java.nio.file.Path getpar = nodeRemove.getPath().getParent();
        if(getpar == null)
            getpar = java.nio.file.Paths.get("");
        Node parent = findNodeRecursively(proj.getRootNode(),getpar.toString());
        parent.getChildren().remove(nodeRemove);
        Logger.log("test deleteFile :" + filename );
        return Response.ok(true).build();
    }

    @POST
    @Path("/delete/folder")
    public Response deleteFolder(RequestEndpoint requestEndpoint) {

        String filename = requestEndpoint.getPath();
        java.nio.file.Path nodePath = java.nio.file.Paths.get(filename);
        if(!Files.exists(nodePath) || !Files.isDirectory(nodePath)){
            Logger.logError("test deleteFolder :" + filename );
            return Response.status(Response.Status.NOT_FOUND).entity("folder dont exist").build();
        }
        Logger.log("test deleteFolder :" + filename );
        Node nodeRemove = findNodeRecursively(proj.getRootNode(),filename);
        Boolean fileNode = projectService.getNodeService().delete(nodeRemove);
        if(fileNode == false){
            Logger.logError("test deleteFolder not working :" + filename );
            return Response.status(Response.Status.NOT_FOUND).entity("folder dont exist").build();
        }
        java.nio.file.Path getpar = nodeRemove.getPath().getParent();
        if(getpar == null)
            getpar = java.nio.file.Paths.get("");
        Node parent = findNodeRecursively(proj.getRootNode(),getpar.toString());
        parent.getChildren().remove(nodeRemove);
        Logger.log("test deleteFolder :" + filename );
        return Response.ok(true).build();
    }

    private Node findNodeRecursively(Node currentNode, String filename) {
        if (currentNode.getPath().toString().equals(filename)) {
            return currentNode;
        }
        for (Node child : currentNode.getChildren()) {
            Node result = findNodeRecursively(child, filename);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @POST
    @Path("/execFeature")
    public Response execFeature(ExecFeatureParams params) {
        String feature = params.getFeature();
        List<String> paramList = params.getParams();
        String projectParam = params.getProject();
        if (proj == null){
            Logger.logError("feature not working: " + feature + " paramList: "+ paramList + " projectParam: " + projectParam);
            return Response.status(Response.Status.NOT_FOUND).entity("file dont exist").build();
        }
        switch (feature) {
            case "CLEANUP":
                proj.getFeature(Mandatory.Features.Any.CLEANUP).get().execute(proj);
                break;
            case "DIST":
                proj.getFeature(Mandatory.Features.Any.DIST).get().execute(proj);
                break;
            case "SEARCH":
                proj.getFeature(Mandatory.Features.Any.SEARCH).get().execute(proj,paramList);
                break;
            case "COMPILE":
                proj.getFeature(Mandatory.Features.Maven.COMPILE).get().execute(proj);
                break;
            case "TEST":
                proj.getFeature(Mandatory.Features.Maven.TEST).get().execute(proj);
                break;
            case "CLEAN":
                proj.getFeature(Mandatory.Features.Maven.CLEAN).get().execute(proj);
                break;
            case "PACKAGE":
                proj.getFeature(Mandatory.Features.Maven.PACKAGE).get().execute(proj);
                break;
            case "INSTALL":
                proj.getFeature(Mandatory.Features.Maven.INSTALL).get().execute(proj);
                break;
            case "EXEC":
                proj.getFeature(Mandatory.Features.Maven.EXEC).get().execute(proj);
                break;
            case "TREE":
                proj.getFeature(Mandatory.Features.Maven.TREE).get().execute(proj);
                break;
            case "PULL":
                proj.getFeature(Mandatory.Features.Git.PULL).get().execute(proj);
                break;
            case "ADD":
                proj.getFeature(Mandatory.Features.Git.ADD).get().execute(proj,paramList);
                break;
            case "COMMIT":
                proj.getFeature(Mandatory.Features.Git.COMMIT).get().execute(proj,paramList);
                break;
            case "PUSH":
                proj.getFeature(Mandatory.Features.Git.PUSH).get().execute(proj);
                break;
            default:

                Logger.logError("feature not working: " + feature + " paramList: "+ paramList + " projectParam: " + projectParam);

                return Response.status(Response.Status.NOT_FOUND).entity("file dont exist").build();
        }

        Logger.log("feature: " + feature + " paramList: "+ paramList + " projectParam: " + projectParam);
        return Response.ok(true).build();

    }


    @POST
    @Path("/move")
    public Response move(MoveParams moveParams) {
        String src = moveParams.getSrc();
        String dst = moveParams.getDst();
        Logger.log("test move src :" + src + " dst : "+dst );
        Node nodeSrc = findNodeRecursively(proj.getRootNode(), src);
        Node nodeDst = findNodeRecursively(proj.getRootNode(), dst);
        if(nodeDst == null || nodeSrc == null){
            Logger.logError("test move not working src :" + src + " dst : "+dst );
            return Response.status(Response.Status.NOT_FOUND).entity("Move Not Working1").build();
        }
        Logger.log("test move src :" + src + " dst : "+dst );

        Node fileNode = projectService.getNodeService().move(
                nodeSrc,
                nodeDst
        );
        if(fileNode == null){
            Logger.logError("test move src :" + src + " dst : "+dst );
            return Response.status(Response.Status.NOT_FOUND).entity("Move Not Working2").build();
        }
        java.nio.file.Path getparSrc = nodeSrc.getPath().getParent();
        if(getparSrc == null)
            getparSrc = java.nio.file.Paths.get("");
        
        Node parentSrc = findNodeRecursively(proj.getRootNode(),getparSrc.toString());


        Node nodeFinal = new NodeImpl(dst+ "/" + nodeSrc.getPath().getFileName().toString(), nodeSrc.getType());

        parentSrc.getChildren().remove(nodeSrc);
        nodeDst.getChildren().add(nodeFinal);
        
        Logger.log("test move src :" + src + " dst : "+dst );
        return Response.ok(true).build();
    }

    @POST
    @Path("/update")
    public Response update(UpdateParams params) {
        String path = params.getPath();
        String content = params.getContent();
        int from = params.getFrom();
        int to = params.getTo();
        java.nio.file.Path projPath = java.nio.file.Paths.get(path);
        if(!Files.exists(projPath) || Files.isDirectory(projPath)){

            Logger.logError("destination node not found! Path: "+ path + " from: " + from +" to: "+ to +" content: " + content);
            return Response.status(Response.Status.NOT_FOUND).entity("file dont exist").build();
        }
        Logger.log("Update successful! Path: "+ path + " from: " + from +" to: "+ to +" content: " + content);

        Node nodePath = findNodeRecursively(proj.getRootNode(), path);
        if (nodePath != null) {
            Node res = projectService.getNodeService().update(nodePath, from, to, content.getBytes());
            if (res == null) {
                Logger.logError("Failed to update file. Path: " + path + " from: " + from +" to: "+ to +" content: " + content);
                return Response.status(Response.Status.NOT_FOUND).entity("file dont exist").build();
            }
            Logger.log("Update successful! Path: "+ path + " from: " + from +" to: "+ to +" content: " + content);
            return Response.ok(true).build();
        } else {
            Logger.logError("destination node not found! Path: "+ path + " from: " + from +" to: "+ to +" content: " + content);
            return Response.status(Response.Status.NOT_FOUND).entity("destination node not found! Update").build();
        }
    }

    @POST
    @Path("/cli")
    public Response commandLine(CommandParams params) {
        String command = params.getCommand();
        StringBuilder output = new StringBuilder();
        Logger.log("try to execute command: " + command);
        String os = System.getProperty("os.name").toLowerCase();

        try {
            Process process;
            if (os.contains("win")) {
                // Pour Windows, utilisez cmd /c
                process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});
            } else {
                // Pour Unix/Linux/Mac, utilisez sh -c
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Lire la sortie de la commande
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Lire la sortie d'erreur de la commande
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Attendre que la commande soit terminée
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("La commande a échoué avec le code de sortie : " + exitCode)
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de l'exécution de la commande : " + e.getMessage())
                    .build();
        }

        return Response.ok(output.toString()).build();
    }


    @GET @Path("/hello")
    public Response helloWorld()
    {
        Logger.log("Saying hello !");
        return Response.ok("Hello World !").build();
    }

    @POST
    @Path("/fileContent")
    public Response updateFileContent(RequestEndpoint params) {
        String path = params.getPath();
        java.nio.file.Path projPath = java.nio.file.Paths.get(path);
        if(!Files.exists(projPath) || Files.isDirectory(projPath)){

            Logger.logError("destination node not found! Path: "+ path );
            return Response.status(Response.Status.NOT_FOUND).entity("file dont exist").build();
        }
        Logger.log("Update successful! Path: "+ path);
       try {
        // Read the file content
        String content = new String(Files.readAllBytes(projPath), StandardCharsets.UTF_8);
        
        Logger.log("File content read successfully! Path: " + path);
        return Response.ok(content).build();
        } catch (IOException e) {
        Logger.logError("Error reading file content! Path: " + path + " Error: " + e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error reading file content").build();
        } 
    }

    @POST
    @Path("/updateContent")
    public Response updateContent(UpdateContentRequest params) {
        String path = params.getPath();
        String content = params.getContent();
        java.nio.file.Path projPath = java.nio.file.Paths.get(path);
        if(!Files.exists(projPath) || Files.isDirectory(projPath)){

            Logger.logError("destination node not found! Path: "+ path );
            return Response.status(Response.Status.NOT_FOUND).entity("file dont exist").build();
        }
        Logger.log("Update successful! Path: "+ path);
        Logger.log("Update request received! Path: " + path);
        try {
            // Write the new content to the file
            Files.write(projPath, content.getBytes(StandardCharsets.UTF_8));
            Logger.log("File content updated successfully! Path: " + path);
            return Response.ok(content).build();
        } catch (IOException e) {
            Logger.logError("Error updating file content! Path: " + path + " Error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating file content").build();
        }
    }
}