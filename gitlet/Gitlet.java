package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
 * The main function of Gitlet can apply.
 *
 * @author chenyuanshan
 */

public class Gitlet implements Serializable {

    /**
     * The directory the user currently working on.
     */
    String directory = System.getProperty("user.dir");

    /**
     * The working directory with gitlet and other directories.
     */
    File workingdirectory = Utils.join(directory, ".gitlet");
    /**
     * The name of the current branch.
     */
    String currentbranch;
    /**
     * Current stage.
     */
    Stage stagearea;
    /**
     * The sha1 value of current commit.
     */
    Commit currentcommit;
    /**
     * Current branch.
     */
    Branch branch;
    /**
     * The path of the directory of Commits.
     */
    File commitdirectory = Utils.join(directory, ".gitlet/commits");
    /**
     * The path of the direcoty of Branches.
     */
    File branchdirectory = Utils.join(directory, ".gitlet/branches");
    /**
     * True if there is merge conflicts.
     */
    HashMap<String, Blob> _untracked;


    /**
     * A new Gitlet that read the local information to keep tracking.
     */
    public Gitlet() {
        try {
            stagearea = Utils.readObject(Utils.join(workingdirectory, "stage"), Stage.class);
            branch = stagearea._branch;
        } catch (IllegalArgumentException e) {
            stagearea = new Stage();
        }
        _untracked = new HashMap<>();
    }

    /**
     * Init the working directory and all the directories that needed to store.
     * Initialize the useful invariable from local information.
     *
     * @throws IOException
     */
    public void init() throws IOException {

        if (workingdirectory.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        workingdirectory.mkdirs();
        Files.createDirectory(Paths.get(".gitlet/commits"));
        Files.createDirectory(Paths.get(".gitlet/branches"));

        Commit initcommit = new Commit("initial commit",
                new HashMap<>(), null);
        String cmSHA1 = initcommit.getSha1();
        currentcommit = initcommit;
        Utils.writeObject(Utils.join(commitdirectory, cmSHA1), initcommit);

        Branch initbranch = new Branch("master", initcommit);
        Utils.writeObject(Utils.join(branchdirectory, initbranch._name), initbranch);

        currentbranch = initbranch._name;
        stagearea.setcurrentbranch(initbranch);
        stagearea.set_branches(initbranch);
        branch = initbranch;
        _untracked = new HashMap<>();
        update();
    }

    /**
     * Add the file into the stage.
     *
     * @param filename the file that be added into gitlet.
     */
    public void add(String filename) {
        File f = Utils.join(directory, filename);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob blb = new Blob(filename);
        blb.setcontents(f);
        String blobsha1 = blb.getSHA1();
        stagearea._delete.remove(filename);
        stagearea._add.put(filename, blb);
        HashMap<String, Blob> temp = stagearea._branch._commit._currentBlob;
        for (Blob b : temp.values()) {
            if (b._blobSHA1.equals(blobsha1)) {
                stagearea._add.remove(filename, blb);
            }
        }
        update();
    }


    /**
     * Commit the staged files with MSG.
     *
     * @param msg the log msg come with the commit.
     */
    public void commit(String msg) {
        if (msg.equals("") || msg.equals(" ")) {
            System.out.println("Please enter a commit message.");
            return;
        }

        if (stagearea._add.isEmpty() && stagearea._delete.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit newc;
        HashMap<String, Blob> t = new HashMap<>();
        t.putAll(stagearea._add);
        newc = new Commit(msg, t, branch._commit._Sha_1);
        String crcommit = newc.getSha1();
        newc._removedfiles.putAll(stagearea._delete);
        currentcommit = newc;
        stagearea._branch.setcommit(newc);
        stagearea.clear();
        update();
        Utils.writeObject(Utils.join(commitdirectory, crcommit), newc);
        Utils.writeObject(Utils.join(branchdirectory, stagearea._branch._name), stagearea._branch);
    }

    /**
     * Commit method for merge commit with MSG, PARENTA and PARENTB.
     *
     */
    public void mergecommit(String msg, String parentA, String parentB) {
        Commit newc = new Commit(msg, new HashMap<String, Blob>(), branch._commit._Sha_1);
        newc.set_merged(true, parentA, parentB);
        String crcommit = newc.getSha1();
        stagearea._branch.setcommit(newc);
        update();
        Utils.writeObject(Utils.join(branchdirectory, stagearea._branch._name), stagearea._branch);
        Utils.writeObject(Utils.join(commitdirectory, crcommit), newc);
    }

    /**
     * Remove the file from the gitlet.
     *
     * @param blobname the name of the file to be removed.
     */
    public void rm(String blobname) {
        boolean flag = false;Commit now = branch._commit;
        if (stagearea._add.containsKey(blobname)) {
            stagearea._add.remove(blobname);
            flag = true;
        }
        else {
            while (now != null) {
                if (now._currentBlob.containsKey(blobname)) {
                    Blob bl = now._currentBlob.get(blobname);
                    stagearea._delete.put(blobname, bl);
                    Utils.restrictedDelete(Utils.join(directory, blobname));
                    flag = true;
                    break;
                }
                if (now._parentSHA1 == null) {
                    break;
                }
                String linked = now._parentSHA1;
                now = Utils.readObject(Utils.join(commitdirectory, linked), Commit.class);
            }

        }
        if (!flag) {
            System.out.println("No reason to remove the file.");
        }
        update();
    }

    /**
     * Return the log messages of current of me.
     */
    public void log() {
        Commit cmt = branch._commit;
        while (cmt != null) {
            logmsg(cmt);
            String linked = cmt._parentSHA1;
            if (linked == null) {
                break;
            }
            cmt = Utils.readObject(Utils.join(commitdirectory, linked),
                    Commit.class);
        }
    }

    /**
     * Print the information of CMT.
     *
     * @param cmt the commit to be print.
     */
    public void logmsg(Commit cmt) {
        System.out.println("===");
        System.out.println("commit " + cmt._Sha_1);
        if (cmt._merged) {
            System.out.println("Merge: " + cmt.parentA_sha1.substring(0, 7) + " " + cmt.parentB_sha1.substring(0, 7));
        }
        System.out.println("Date: " + cmt._date);
        System.out.println(cmt._logMSG);
        System.out.println();
    }

    /**
     * Print all the information of all branches.
     */
    public void globallog() {
        List<String> names = Utils.plainFilenamesIn(commitdirectory);
        if (names != null) {
            for (String n : names) {
                Commit c = Utils.readObject(Utils.join(commitdirectory, n), Commit.class);
                logmsg(c);
            }
        }
    }

    /**
     * Find the commit with COMMITMSG.
     *
     * @param commitmsg the commit message.
     *                 of the commit we want to find.
     */
    public void find(String commitmsg) {
        boolean flag = false;
        List<String> names = Utils.plainFilenamesIn(commitdirectory);
            for (String n : Utils.plainFilenamesIn(commitdirectory)) {
                Commit c = Utils.readObject(Utils.join(commitdirectory, n), Commit.class);
                if (c._logMSG.equals(commitmsg)) {
                    System.out.println(c._Sha_1);
                    flag = true;
                }
            }
        if (!flag) {
            System.out.println("Found no commit with that message.");
        }

    }

    /**
     * Print the status of me.
     */
    public void status() {
        //branches.
        System.out.println("=== Branches ===");
        System.out.println("*" + stagearea._branch._name);
        for (Branch b : stagearea._allbranches.values()) {
            if (!b._name.equals(stagearea._branch._name)) {
                System.out.println(b._name);
            }
        }
        System.out.println();
        //Staged files.
        System.out.println("=== Staged Files ===");
        ArrayList<String> stagedfile = new ArrayList<>(stagearea._add.keySet());
        Collections.sort(stagedfile);
        for (String st : stagedfile) {
            System.out.println(stagearea._add.get(st)._filename);
        }
        System.out.println();
        //removed files.
        System.out.println("=== Removed Files ===");
        ArrayList<String> removed = new ArrayList<>(stagearea._delete.keySet());
        Collections.sort(removed);
        for (String st : removed) {
            System.out.println(stagearea._delete.get(st)._filename);
        }
        System.out.println();
        //extra credit.
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();

    }

    /**
     * Check out specific file named FILENAME.
     *
     * @param filename the file needed to be undo.
     */
    public void checkout(String filename) {
        boolean flag = false;
        Blob temp = null;
        Commit cmt = branch._commit;
        HashMap<String, Blob> blobfiles = cmt._currentBlob;
        for (String s : blobfiles.keySet()) {
            if (filename.equals(s)) {
                File f = new File(filename);
                String contents = blobfiles.get(s)._contentstring;
                Utils.writeContents(f, contents);
                temp = new Blob(f.getName());
                temp.setcontents(f);
                flag = true;
                break;
            }
        }
        if (!flag) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        update();
    }

    /**
     * Check out the target file with given COMMITID.
     *
     * @param commitid the specific SHA1.
     * @param filename the specific file name.
     */
    public void checkout(String commitid, String filename) {
        boolean flag = false;
        boolean flaga = false;
        Blob temp = null;
        String fullname = "";
        List<String> names = Utils.plainFilenamesIn(commitdirectory);
        if (names == null) {
            System.out.println("No commit with that id exists.");
            return;
        } else {
            for (String name : names) {
                if (name.substring(0, commitid.length()).equals(commitid)) {
                    flaga = true;
                    fullname = name;
                    break;
                }
            }
        }
        if (!flaga) {
            System.out.println("No commit with that id exists.");
            return;
        } else {
            Commit cmt = Utils.readObject(Utils.join(commitdirectory, fullname), Commit.class);
            HashMap<String, Blob> blobfiles = cmt._currentBlob;
            for (String s : blobfiles.keySet()) {
                if (filename.equals(s)) {
                    File f = new File(filename);
                    String contents = blobfiles.get(s)._contentstring;
                    Utils.writeContents(f, contents);
                    temp = new Blob(f.getName());
                    temp.setcontents(f);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                System.out.println("File does not exist in that commit.");
                return;
            }
        }
        update();
    }

    /**
     * Undo the whole branch.
     *
     * @param branchname the name of branch.
     */
    public void checkbranch(String branchname) {
        if (!stagearea._allbranches.containsKey(branchname)) {
            System.out.println("No such branch exists.");
            return;
        }
        Branch targetbranch = Utils.readObject(Utils.join(branchdirectory, branchname), Branch.class);
        Commit cmt = targetbranch._commit;
        HashMap<String, Blob> blobfiles = cmt._currentBlob;
        if (stagearea._branch._name.equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        if (findUntracked()) {
            for (Blob b : _untracked.values()) {
                if (cmt._currentBlob.containsKey(b._filename)) {
                    System.out.println("There is an untracked file in the way; delete it or add it first.");
                    return;
                }
            }

        }Commit currentc = stagearea._branch._commit;
        for (String s : blobfiles.keySet()) {
            File f = new File(s);
            String contents = cmt._currentBlob.get(s)._contentstring;
            Utils.writeContents(f, contents);
        }
        for (Blob b : currentc._currentBlob.values()) {
            if (!cmt._currentBlob.containsKey(b._filename)) {
                Utils.restrictedDelete(Utils.join(directory, b._filename));
            }
        }
        Commit temp = currentc;
        while (temp != null) {
            if (!temp._removedfiles.isEmpty()) {
                for (String n : temp._removedfiles.keySet()) {
                    Utils.restrictedDelete(Utils.join(directory,n));
                }
            }
            if (temp._parentSHA1 == null) {
                break;
            }
            temp = Utils.readObject(Utils.join(commitdirectory,temp._parentSHA1),Commit.class);
        }
        stagearea.clear();
        stagearea.set_branches(targetbranch);
        stagearea.setcurrentbranch(targetbranch);
        stagearea._branch._commit = cmt;
        currentbranch = targetbranch._name;
        currentcommit = cmt;
        Utils.writeObject(Utils.join(branchdirectory, stagearea._branch._name), stagearea._branch);
        update();
    }

    /**
     * Whether there are untracked files.
     *
     * @return true if there is untracked files.
     */
    public boolean findUntracked() {
        List<String> allcommits =
                Utils.plainFilenamesIn(commitdirectory);
        ArrayList<HashMap<String, Blob>> allfiles = new ArrayList<>();
        for (String c : allcommits) {
            Commit temp = Utils.readObject(Utils.join(commitdirectory, c), Commit.class);
            allfiles.add(temp._currentBlob);
        }
        List<String> names = Utils.plainFilenamesIn(directory);
        int[] _find = new int[names.size()];
        Arrays.fill(_find, 0);
        int count = 0;
        for (String n : names) {
            if (n.equals("gitlet-design.txt") || !n.substring(n.length() - 4).equals(".txt")) {
                _find[count] = -1;
                count = count + 1;
            } else {
                if (stagearea._add.containsKey(n)) {
                    Blob filebolb = new Blob(n);
                    File f = Utils.join(directory, n);
                    filebolb.setcontents(f);
                    if (filebolb._contentstring.equals(stagearea._add.get(n)._contentstring)) {
                        _find[count] = 1;
                        break;
                    }

                } else {
                    for (HashMap<String, Blob> hm : allfiles) {
                        if (hm.containsKey(n)) {
                            Blob filebolb = new Blob(n);
                            File f = Utils.join(directory, n);
                            filebolb.setcontents(f);
                            if (filebolb._contentstring.equals(hm.get(n)._contentstring)) {
                                _find[count] = 1;
                                break;
                            }
                        }
                    }
                }
                count = count + 1;
            }
        }
        boolean flag = false;
        for (int i = 0; i < _find.length; i = i + 1) {
            if (_find[i] == 0) {
                File f = Utils.join(directory, names.get(i));
                Blob filebolb = new Blob(names.get(i));
                filebolb.setcontents(f);
                _untracked.put(filebolb._filename,filebolb);
                flag = true;
            }
        }
        if (flag) {
            return true;
        }
        return false;
    }

    /**
     * Create a new branch of me.
     *
     * @param branchname the name of the branch.
     */
    public void branch(String branchname) {
        List<String> names = Utils.plainFilenamesIn(branchdirectory);
        if (stagearea._allbranches.containsKey(branchname)) {
            System.out.println("A branch with that name already exists.");
        } else {
            Commit n = stagearea._branch._commit;
            Branch newbranch = new Branch(branchname, n);
            stagearea.set_branches(newbranch);
            Utils.writeObject(Utils.join(branchdirectory, newbranch._name), newbranch);
            update();
        }
    }

    /**
     * Remove the branch from this Gitlet.
     *
     * @param branchname the name of the target branch.
     */
    public void rm_branch(String branchname) {
        if (branchname.equals(stagearea._branch._name)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        List<String> names = Utils.plainFilenamesIn(branchdirectory);
        if (!stagearea._allbranches.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            Branch b = stagearea._allbranches.get(branchname);
            b.setcommit(null);
            stagearea.removebranch(branchname);
            update();
        }

    }

    /**
     * Reset me to the status of the Commit with id of COMMITID.
     *
     * @param commitid the id of target commit.
     */
    public void reset(String commitid) {
        boolean flag = false;
        List<String> names = Utils.plainFilenamesIn(commitdirectory);
        Commit ct = null;
        if (names != null && names.contains(commitid)) {
            flag = true;
            ct = Utils.readObject(Utils.join(commitdirectory, commitid), Commit.class);
        }
        if (!flag) {
            System.out.println("No commit with that id exists.");
            return;
        }
        if (findUntracked()) {
            for (Blob b : _untracked.values()) {
                if (ct._currentBlob.containsKey(b._filename)) {
                    System.out.println("There is an untracked file in the way; delete it or add it first.");
                    return;
                }
            }
        }
        for (String filename : ct._currentBlob.keySet()) {
            checkout(commitid, filename);
        }
        Commit now = branch._commit;
        while (!now._Sha_1.equals(ct._Sha_1)) {
            for (String f : now._currentBlob.keySet()) {
                Boolean t = Utils.restrictedDelete(Utils.join(directory, now._currentBlob.get(f)._filename));
            }
            String linked = now._parentSHA1;
            if (linked == null) {
                break;
            }
            now = Utils.readObject(Utils.join(commitdirectory, linked), Commit.class);
        }
        stagearea._branch.setcommit(ct);
//        branch.set_commit(ct);
//        stagearea.set_branches(branch);
        stagearea.clear();
        Utils.writeObject(Utils.join(branchdirectory, stagearea._branch._name), stagearea._branch);
        update();
    }

    /**
     * Merge current branch with branch with name of BRANCHNAME.
     */
    public void merge(String branchname) {
        if (stagearea._branch._name.equals(branchname)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (!stagearea._add.isEmpty() || !stagearea._delete.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!stagearea._allbranches.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (findUntracked()) {
            System.out.println("There is an untracked file in the way; delete it or add it first. ");
        }
        //find splitPoint.
        Commit splitPoint = null;
        String sha1 = "";
        Commit target = stagearea._allbranches.get(branchname)._commit;
        Commit now = stagearea._branch._commit;
        ArrayList<String> commitArray = new ArrayList<>();
        while (now != null) {
            commitArray.add(now._Sha_1);
            if (now._parentSHA1 == null) {
                break;
            }
            now = Utils.readObject(Utils.join(commitdirectory, now._parentSHA1), Commit.class);
        }
        Commit tt = target;
        while (tt != null) {
            if (commitArray.contains(tt._Sha_1)) {
                splitPoint = tt;
                break;
            }
            if (tt._parentSHA1 == null) {
                break;
            }
            tt = Utils.readObject(Utils.join(commitdirectory, tt._parentSHA1), Commit.class);
        }
        // decide fail case.
        if (splitPoint != null && splitPoint._Sha_1.equals(target._Sha_1)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        } else if (splitPoint != null
                && splitPoint._Sha_1.equals(stagearea._branch._commit._Sha_1)) {
            checkbranch(branchname);
            System.out.println("Current branch fast-forwarded.");
        } else {
            HashMap<String, Blob> currenttrack = new HashMap<>();
            Commit current = stagearea._branch._commit;
            while (!current._Sha_1.equals(splitPoint._Sha_1)) {
                currenttrack.putAll(current._currentBlob);
                if (current._parentSHA1 == null) {
                    break;
                }
                current = Utils.readObject(Utils.join(commitdirectory, current._parentSHA1), Commit.class);
            }
            Commit crt = current;
            Commit t = target;
            boolean marked = false;
            while (!t._Sha_1.equals(splitPoint._Sha_1)) {
                for (String s : t._currentBlob.keySet()) {
                    if (currenttrack.containsKey(s)) {
                        marked = true;
                        File f = new File(s);
                        StringBuilder cts = new StringBuilder();
                        cts.append("<<<<<<< HEAD\n");
                        if (!currenttrack.get(s)._contentstring.isEmpty()
                                && !t._currentBlob.get(s)._contentstring.isEmpty()) {
                            String content = currenttrack.get(s)._contentstring;
                            cts.append(content);
                            cts.append("=======\n");
                            content = t._currentBlob.get(s)._contentstring;
                            cts.append(content);
                        } else if (!t._currentBlob.get(s)._contentstring.isEmpty()) {
                            String content = currenttrack.get(s)._contentstring;
                            cts.append(content);
                            cts.append("=======\n");
                        } else if (!currenttrack.get(s)._contentstring.isEmpty()) {
                            String content = t._currentBlob.get(s)._contentstring;
                            cts.append(content);
                            cts.append("=======\n");
                        }
                        cts.append(">>>>>>>\n");
                        Utils.writeContents(f,cts.toString());
                    }
                }
                t = Utils.readObject(Utils.join(commitdirectory,t._parentSHA1),Commit.class);
            }
            t = target;
            while (!t._Sha_1.equals(splitPoint._Sha_1)) {
                for (String s : t._removedfiles.keySet()) {
                    if (currenttrack.containsKey(s)) {
                        marked = true;
                        File ff = new File(s);
                        StringBuilder cts = new StringBuilder();
                        cts.append("<<<<<<< HEAD\n");
                        if (!currenttrack.get(s)._contentstring.isEmpty()
                                && !t._removedfiles.get(s)._contentstring.isEmpty()) {
                            String content = currenttrack.get(s)._contentstring;
                            cts.append(content);
                            cts.append("=======\n");
                        } else if (!t._removedfiles.get(s)._contentstring.isEmpty()) {
                            String content = currenttrack.get(s)._contentstring;
                            cts.append(content);
                            cts.append("=======\n");
                        } else if (!currenttrack.get(s)._contentstring.isEmpty()) {
                            cts.append("=======\n");
                        }
                        cts.append(">>>>>>>\n");
                        Utils.writeContents(ff,cts.toString());
                    }
                }
                t = Utils.readObject(Utils.join(commitdirectory,t._parentSHA1),Commit.class);
            }
            if (marked) {
                System.out.println("Encountered a merge conflict.");
            }
            for (String s : target._currentBlob.keySet()) {
                if (!currenttrack.containsKey(s)) {
                    Blob bt = target._currentBlob.get(s);
                    Utils.writeContents(Utils.join(directory, s), bt._contentstring);
                }
            }

            String mergemsg = "Merged " + branchname + " into " + branch._name + ".";
            mergecommit(mergemsg, branch._commit._Sha_1, stagearea._allbranches.get(branchname)._commit._Sha_1);
        }
    }


    /**
     * Update the local information of stage.
     */
    public void update() {
        Utils.writeObject(Utils.join(workingdirectory, "stage"), stagearea);
    }

}

