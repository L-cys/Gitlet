package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A class to record the branch information.
 * Also the place to store the files tobe commit
 * and to be deleted.
 *
 * @author chenyuanshan
 */

public class Stage implements Serializable {

    /**
     * The HashMap for the newly added, or,
     * the staged files.
     */
    HashMap<String, Blob> _add;
    /**
     * The HashMap for deleted files.
     */
    HashMap<String, Blob> _delete;
    /**
     * Current branch of this stage.
     */
    Branch _branch;
    /**
     * A HashMap for all branch.
     */
    HashMap<String, Branch> _allbranches;

    /**
     * Initialize a new Stage.
     */
    public Stage() {
        _add = new HashMap<>();
        _delete = new HashMap<>();
        _allbranches = new HashMap<>();

    }

    /**
     * Clear the current stage.
     */
    public void clear() {
        _add = new HashMap<>();
        _delete = new HashMap<>();
    }

    /**
     * Set my cerrent branch.
     *
     * @param branch new current branch.
     */
    public void setcurrentbranch(Branch branch) {
        _branch = branch;
    }

    /**
     * Add NEWBRANCH into my branch HashMap.
     */
    public void set_branches(Branch newbranch) {
        _allbranches.put(newbranch._name, newbranch);
    }

    /**
     * Remove BRANCHNAME from my HashMap.
     */
    public void removebranch(String branchname) {
        _allbranches.remove(branchname);
    }

}
