package gitlet;


import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        int length = args.length;
        if(length == 0) {
            exitWithError("Please enter a command.");
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                Repository.status();
                break;
            case "checkout":
                if (length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else if (length == 3 && args[1].equals("--")) {
                    Repository.checkout(args[2]);
                } else if (length == 4 && args[2].equals("--")) {
                    Repository.checkout(args[1], args[3]);
                } else {
                    exitWithError("Incorrect operands.");
                }
                break;
            case "branch":
                validateNumArgs(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                exitWithError("No command with that name exists.");
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            exitWithError("Incorrect operands.");
        }
    }
}
