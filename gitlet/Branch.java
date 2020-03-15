package gitlet;

import java.io.Serializable;

/**
 * The class to hold a branch of commits.
 *
 * @author chenyuanshan
 */

public class Branch implements Serializable {

    /**
     * The name of this branch.
     */
    String _name;
    /**
     * The current commit, or head, of this branch.
     */
    Commit _commit;

    /**
     * Initilize a new branch.
     *
     * @param branchname the name of me.
     * @param commit     the commit of me.
     */
    public Branch(String branchname, Commit commit) {
        _name = branchname;
        _commit = commit;
    }

    /**
     * Change my head to COMMIT.
     */
    public void setcommit(Commit commit) {
        _commit = commit;
    }


}
