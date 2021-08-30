package org.bundleproject.discover.repo.entry;

public class EntryAction {

    private final String display;
    private final String creator;
    private final Runnable action;

    public EntryAction(String display, String creator, Runnable action) {
        this.display = display;
        this.creator = creator;
        this.action = action;
    }

    public String getDisplay() {
        return display;
    }

    public String getCreator() {
        return creator;
    }

    public Runnable getAction() {
        return action;
    }
}
