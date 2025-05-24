package fr.epita.assistants.myide.presentation.rest;

import java.util.List;

public class ExecFeatureParams {
    private String feature;
    private List<String> params;
    private String project;

    // Getters and setters
    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}