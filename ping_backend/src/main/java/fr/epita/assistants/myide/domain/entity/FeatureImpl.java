package fr.epita.assistants.myide.domain.entity;

import fr.epita.assistants.myide.utils.Logger;
import org.apache.lucene.util.fst.PairOutputs;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class FeatureImpl implements Feature{

    Mandatory.Features.Any AnyFeature;
    Mandatory.Features.Maven MavenFeature;
    Mandatory.Features.Git GitFeature;

    record ExecutionReport(boolean isSuccess, List<Node> searchResult) implements Feature.ExecutionReport {
        @Override
        public boolean isSuccess() {
            return isSuccess;
        }

        public List<Node> getResults() {
            return searchResult;
        }
    }

    boolean search(String str,List<Node> res, Node node )
    {
        List<Node> L = node.getChildren();
        Pattern pattern = Pattern.compile(".*\\b" + Pattern.quote(str) + "\\b.*");
        if(str.contains(node.getPath().getFileName().toString()))
        {
            res.add(node);
        }else {

        }
        for (int i = 0; i < L.size(); i++)
       {

           if(L.get(i).isFolder())
           {
               search(str,res,L.get(i));
           }
           else
           {
               if(str.contains(L.get(i).getPath().getFileName().toString()))
               {
                   res.add(L.get(i));
               }
               else
                {
                   File file = new File(String.valueOf(L.get(i).getPath()));

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        boolean found = false;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains(str) || line.equals(str)) {
                                res.add(L.get(i));
                                found = true;
                                break;
                            }
                        }

                   }
                   catch (IOException e) {

                   }
               }
           }
       }
       return(!L.isEmpty());
    }
    boolean git_add(Git git,String str, Project project)
    {
        Path path = Paths.get(project.getRootNode().getPath().toString() +"/" +str);
        if (Files.exists(path))
        {
            //Logger.log("Lestgooooooo: " + project.getRootNode().getPath().toString() +"/" +str);
            if (Files.isDirectory(path)) {
                File directory = path.toFile();
                File[] list_Files = directory.listFiles();
                if (list_Files != null) {
                    for (File file : list_Files) {
                        if (!git_add(git, file.toString(),project))
                        {
                            return false;
                        }
                    }
                }
            }
            try {
                git.add().addFilepattern(str).call();
            } catch (GitAPIException e) {
                return false;
            }
            return true;
        }
        return false;
    }
    public FeatureImpl(Mandatory.Features.Any type) {
        this.AnyFeature = type;
        this.MavenFeature = null;
        this.GitFeature = null;
    }

    public FeatureImpl(Mandatory.Features.Maven type) {
        this.MavenFeature = type;
        this.AnyFeature = null;
        this.GitFeature = null;
    }
    public FeatureImpl(Mandatory.Features.Git type) {
        this.GitFeature = type;
        this.MavenFeature = null;
        this.AnyFeature = null;
    }


    boolean Zip_project(Path source,Path zip)
    {
        try (ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(zip.toFile())))
        {
            Files.walk(source).filter(path -> !Files.isDirectory(path)).forEach(path ->
            {
                ZipEntry zipEntry = new ZipEntry(source.relativize(path).toString());
                try {
                    zo.putNextEntry(zipEntry);
                    Files.copy(path,zo);
                    zo.closeEntry();
                } catch (IOException e) {
                }
            });
        }
        catch (IOException e)
        {
            return  false;
        }
        return true;
    }
    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /*
    Boolean SearchClean(Node root ,Node father ,String name)
    {
        Logger.log(root.getPath().toString());

        List<Node> toRemove = new ArrayList<>();

        if (root.getPath().getFileName().toString().equals(name))
        {
            if (root.isFolder())
            {
                deleteDirectory(new File(root.getPath().toString()));
            }
            else
            {
                try
                {
                    Files.delete(root.getPath());
                }
                catch (IOException e) {
                    return false;
                }
            }
            if(father != null)
            {
                toRemove.add(root);
            }
        }
        else
        {
            Iterator<Node> iterator = root.getChildren().iterator();
            while (iterator.hasNext())
            {
                Node child = iterator.next();
                if (!SearchClean(child, root, name)) {
                    SearchClean(child, root, name);
                }
            }
        }

        for(Node node : toRemove)
        {
            father.getChildren().remove(node);
        }
        return true;
    }
    */
    Boolean SearchClean(Node root, Node father, String name) {
        Logger.log(root.getPath().toString());

        Queue<List<Node>> toDelete = new LinkedList<>();

        collectNodesToDelete(root, name, toDelete, null);

        // Maintenant supprimez les nœuds collectés
        for (List<Node> couple : toDelete) {
            deleteNode(couple);
            if (couple.get(1) != null) {
                couple.get(1).getChildren().remove(couple.get(0));
            }
            else
            {
                //le fichier a supprimer est la racine, il faut mettre le rootnode du projet a null mais on peut pas.
            }
        }
        return true;
    }

    void collectNodesToDelete(Node root, String name, Queue<List<Node>> toDelete, Node father) {
        if (root.getPath().getFileName().toString().equals(name)) {
            List<Node> list = new ArrayList<>();
            list.add(root);
            list.add(father);
            toDelete.add(list);
        } else {
            for (Node child : root.getChildren()) {
                collectNodesToDelete(child, name, toDelete, root);
            }
        }
    }

    void deleteNode(List<Node> node) {
        Node root = node.get(0);
        if (root.isFolder()) {
            deleteDirectory(new File(root.getPath().toString()));
        } else {
            try {
                Files.delete(root.getPath());
            } catch (IOException e) {
                Logger.log("Failed to delete file: " + root.getPath());
            }
        }
    }
    @Override
    public ExecutionReport execute(Project project, Object... params) {
        if (AnyFeature != null) {
            try {
                switch (AnyFeature) {
                    case CLEANUP -> {
                        File list = null;
                        for (Node child : project.getRootNode().getChildren())
                        {
                            if (child.getPath().getFileName().toString().equals(".myideignore"))
                            {
                                list = new File(child.getPath().toString());
                            }
                        }
                        if (list != null) {
                            List<String> lines = Files.readAllLines(list.toPath());
                            for (String line : lines) {
                                if (!SearchClean(project.getRootNode(), null, line))
                                {
                                    return new ExecutionReport(false, null);
                                }
                            }
                        } else {
                            return new ExecutionReport(false, null);
                        }
                    }
                    case DIST -> {
                        project.getFeature(Mandatory.Features.Any.CLEANUP).get().execute(project);
                        if(!Zip_project(project.getRootNode().getPath(), Path.of(project.getRootNode().getPath().toString() + ".zip")))
                        {
                            return new ExecutionReport(false, null);
                        }


                    }
                    case SEARCH -> {

                        Node rootNode = project.getRootNode();
                        Logger.log(rootNode.getPath().toString());
                        List<Node> res = new ArrayList<>();
                       // List<String> p = (List<String>) params[0];
                        //String str = p.get(0);
                        String str = String.valueOf(params[0]);
                        //Logger.log(str);
                        boolean ress = search(str,res,rootNode);
                        for (Node n : res)
                        {
                            Logger.log("SEARCH RESULT :" + n.getPath().toString());
                        }
                        return new ExecutionReport(ress, res);
                    }
                }
            }
            catch (IOException | SecurityException e)
            {
                return new ExecutionReport(false, null);
            }
        }
        if (MavenFeature != null) {
            switch (MavenFeature) {
                case COMPILE -> {
                    //Logger.log("COMPILEEE SIIUUUU");
                    //Logger.log(project.getRootNode().getPath().toAbsolutePath().toString() + "/pom.xml");
                    /*
                    String pat = project.getRootNode().getPath().toAbsolutePath().toString() + "/pom.xml";
                    Process process = Runtime.getRuntime().exec("nvm -f " + pat + " compile", null, project.getRootNode().getPath().toAbsolutePath().toFile());
                     */
                    ProcessBuilder processBuilder = new ProcessBuilder("mvn","compile");
                    processBuilder.directory(project.getRootNode().getPath().toFile());
                    try {
                        Process process = processBuilder.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        return new ExecutionReport(false, null);
                    }
                }
                case CLEAN -> {
                    //Runtime.getRuntime().exec("nvm clean", null, project.getRootNode().getPath().toFile());
                    ProcessBuilder processBuilder = new ProcessBuilder("mvn","clean");
                    //processBuilder.command("sh", "-c", "mvn clean");
                    processBuilder.directory(new File(project.getRootNode().getPath().toString()));
                    try {
                        Process process = processBuilder.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        return new ExecutionReport(false, null);
                    }
                }
                case TEST -> {
                    //Runtime.getRuntime().exec("nvm test", null, project.getRootNode().getPath().toFile());
                    ProcessBuilder processBuilder = new ProcessBuilder("mvn","test");
                   //processBuilder.command("sh", "-c", "mvn test");
                    processBuilder.directory(new File(project.getRootNode().getPath().toString()));
                    try {
                        Process process = processBuilder.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        return new ExecutionReport(false, null);
                    }
                }
                case PACKAGE -> {
                    //Runtime.getRuntime().exec("nvm package", null, project.getRootNode().getPath().toFile());
                    ProcessBuilder processBuilder = new ProcessBuilder("mvn","package");
                    //processBuilder.command("sh", "-c", "mvn package");
                    processBuilder.directory(new File(project.getRootNode().getPath().toString()));
                    try {
                        Process process = processBuilder.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        return new ExecutionReport(false, null);
                    }
                }
                case INSTALL -> {
                    //Runtime.getRuntime().exec("nvm install", null, project.getRootNode().getPath().toFile());
                    ProcessBuilder processBuilder = new ProcessBuilder("mvn","install");
                   // processBuilder.command("sh", "-c", "mvn install");
                    processBuilder.directory(new File(project.getRootNode().getPath().toString()));
                    try {
                        Process process = processBuilder.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        return new ExecutionReport(false, null);
                    }
                }
                case EXEC -> {
                    //Runtime.getRuntime().exec("nvm exec:java", null, project.getRootNode().getPath().toFile());
                    ProcessBuilder processBuilder = new ProcessBuilder("mvn","exec:java");
                   // processBuilder.command("sh", "-c", "mvn exec:java");
                    processBuilder.directory(new File(project.getRootNode().getPath().toString()));
                    try {
                        Process process = processBuilder.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        return new ExecutionReport(false, null);
                    }
                }
                case TREE -> {
                    //Runtime.getRuntime().exec("nvm dependency:tree", null, project.getRootNode().getPath().toFile());
                    ProcessBuilder processBuilder = new ProcessBuilder("mvn","dependency:tree");
                    //processBuilder.command("sh", "-c", "mvn dependency:tree");
                    processBuilder.directory(new File(project.getRootNode().getPath().toString()));
                    try {
                        Process process = processBuilder.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        return new ExecutionReport(false, null);
                    }
                }
            }
        }
        if (GitFeature != null) {

            try {
                File repo = new File(project.getRootNode().getPath().toString() + "/.git");
                Git git = Git.open(repo);
                switch (GitFeature) {
                    case PULL -> {
                        git.pull().setFastForward(MergeCommand.FastForwardMode.FF).call();
                    }
                    case ADD -> {
                        if (!git_add(git, String.valueOf(params[0]),project))
                        {
                            throw new IOException();
                        }
                        /*List<String> list = (List<String>) params[0];
                        for (int i = 0; i < list.size(); i++)
                        {
                            Logger.log("TEST LIST ADD : " + list.get(i));
                            if (!git_add(git,list.get(i),project))
                            {
                                throw new IOException();
                            }
                        }*/
                    }
                    case COMMIT -> {
                        /*List<String> list = (List<String>) params[0];
                        git.commit().setMessage(list.get(0)).call();*/
                        git.commit().setMessage(String.valueOf(params[0])).call();
                    }
                    case PUSH -> {
                        git.push().call();
                    }
                }
            }
            catch (GitAPIException | IOException e) {
                return new ExecutionReport(false, null);
            }
        }
        return new ExecutionReport(true, null);
    }

    @Override
    public Feature.Type type() {
        if (AnyFeature != null)
            return AnyFeature;
        if (MavenFeature != null)
            return MavenFeature;
        if (GitFeature != null)
            return GitFeature;
        return null;// not reached
    }

}