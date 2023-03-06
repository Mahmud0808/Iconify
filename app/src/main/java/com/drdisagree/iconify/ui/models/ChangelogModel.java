package com.drdisagree.iconify.ui.models;

public class ChangelogModel {

    private String title, changes;

    public ChangelogModel(String title, String changes) {
        this.title = title;
        this.changes = changes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }
}
