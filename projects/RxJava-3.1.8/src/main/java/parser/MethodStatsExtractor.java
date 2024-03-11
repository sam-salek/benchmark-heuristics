package parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that takes in a method of type MethodDeclaration and returns a Map<String, Integer> of method stats.
 * E.g. stats: number of loops, number of conditionals, etc.
 *
 * Example usage:
 * Map<String, Integer> stats = MethodStatsExtractor.getStats(method);
 * System.out.println(stats);
 *
 * @author Malte Åkvist
 */
public class MethodStatsExtractor {

    // Private constructor. Use methods directly since they static.
    private MethodStatsExtractor(){}

    /**
     * Get method stats from the provided method.
     *
     * @param method The method to extract stats from.
     * @return A map containing all extracted method stats, with the map key corresponding to a type of stat.
     */
    public static Map<String, Integer> getStats(MethodDeclaration method) {
        Map<String, Integer> methodStats = new HashMap<>();

        methodStats.put("Loops", countLoops(method));
        methodStats.put("Nested loops", countNestedLoops(method));
        methodStats.put("Conditionals", countConditionals(method));
        return methodStats;
    }

    // Counts conditionals in a method
    private static int countConditionals(MethodDeclaration startMethod) {
        int ifCount = startMethod.findAll(IfStmt.class).size(); // Count if and else if statements

        // Count switch case statements, excludes default case
        int switchCaseCount = startMethod.findAll(SwitchStmt.class).stream()
                .flatMap(switchStmt -> switchStmt.getEntries().stream())
                .filter(switchEntry -> switchEntry.getLabels().isNonEmpty()) // Filter out default cases
                .mapToInt(switchEntry -> 1)
                .sum();

        return ifCount + switchCaseCount;
    }

    // Counts loops in a method
    private static int countLoops(MethodDeclaration startMethod) {
        List<Node> loops = startMethod.findAll(Node.class, n ->
                n instanceof ForStmt || n instanceof WhileStmt || n instanceof DoStmt);
        return loops.size();
    }

    // Counts nested loops in a method
    private static int countNestedLoops(MethodDeclaration startMethod) {
        return calculateDepth(startMethod, 0) - 1;
    }

    // Counts depth of nested loops in a method
    private static int calculateDepth(Node node, int depth) {
        int maxDepth = depth;
        for (Node child : node.getChildNodes()) {
            int childDepth;
            if (child instanceof ForStmt || child instanceof WhileStmt || child instanceof DoStmt) {
                childDepth = calculateDepth(child, depth + 1);
            } else {
                childDepth = calculateDepth(child, depth);
            }
            maxDepth = Math.max(maxDepth, childDepth);
        }
        return maxDepth;
    }
}