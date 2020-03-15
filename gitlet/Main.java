package gitlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author chenyuanshan
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) throws IOException {
        String f;
        try {
            f = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Please enter a command."); return;
        }
        String[] allcommand = {"add", "commit", "branch", "merge", "checkout", "rm", "find",
                "log", "global-log", "init", "status", "rm-branch", "reset"};
        ArrayList<String> allcommands = new ArrayList<>(Arrays.asList(allcommand));
        if (!allcommands.contains(f)) {
            System.out.println("No command with that name exists."); return;
        }
        String s = null; String t = null;
        if (args.length > 1) {
            s = args[1];
        }
        if (args.length > 2) {
            t = args[2];
        }
        Gitlet gitlet = new Gitlet();
        if (f.equals("init")) {
            gitlet.init();
        } else if (f.equals("log")) {
            gitlet.log();
        } else if (f.equals("global-log")) {
            gitlet.globallog();
        } else if (!gitlet.workingdirectory.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        } else if (f.equals("status")) {
            gitlet.status();
        } else {
            switch (f) {
            case "add": gitlet.add(s); break;
            case "commit":
                if (s == null) {
                    System.out.println("Please enter a commit message."); return;
                }
                gitlet.commit(s); break;
            case "rm": gitlet.rm(s); break;
            case "find": gitlet.find(s); break;
            case "checkout":
                if (s != null && s.equals("--") && t != null) {
                    gitlet.checkout(t);
                } else if (args.length > 3 && t.equals("--")) {
                    String fo = args[3];
                    gitlet.checkout(s, fo);
                } else if (t == null) {
                    gitlet.checkbranch(s);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "branch": gitlet.branch(s); break;
            case "rm-branch":
                if (s != null) {
                    gitlet.rm_branch(s);
                }
                break;
            case "reset": gitlet.reset(s); break;
            case "merge": gitlet.merge(s); break;
            default: System.out.println("Incorrect operands.");
            }
        }
    }

    public void mainhelper(Gitlet gitlet, String... args) throws IOException {
        String f;
        try {
            f = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Please enter a command."); return;
        }
        String s = null; String t = null;
        if (args.length > 1) {
            s = args[1];
        }
        if (args.length > 2) {
            t = args[2];
        }
        switch (f) {
            case "add": gitlet.add(s); break;
            case "commit":
                if (s == null) {
                    System.out.println("Please enter a commit message."); return;
                }
                gitlet.commit(s); break;
            case "rm": gitlet.rm(s); break;
            case "find": gitlet.find(s); break;
            case "checkout":
                if (s != null && s.equals("--") && t != null) {
                    gitlet.checkout(t);
                } else if (args.length > 3 && t.equals("--")) {
                    String fo = args[3];
                    gitlet.checkout(s, fo);
                } else if (t == null) {
                    gitlet.checkbranch(s);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "branch": gitlet.branch(s); break;
            case "rm-branch":
                if (s != null) {
                    gitlet.rm_branch(s);
                }
                break;
            case "reset": gitlet.reset(s); break;
            case "merge": gitlet.merge(s); break;
            default: System.out.println("Incorrect operands.");
        }
    }

}
