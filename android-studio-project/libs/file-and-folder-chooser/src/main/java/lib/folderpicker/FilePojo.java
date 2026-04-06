package lib.folderpicker;

import java.util.Comparator;

/**
 * Created by Kashif on 4/7/2018.
 */

public class FilePojo {

    private String name;
    private boolean folder;

    public FilePojo(String name, boolean folder){
        this.name = name;
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

}
